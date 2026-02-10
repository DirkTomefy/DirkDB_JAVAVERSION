package sqlTsinjo.socket.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import sqlTsinjo.config.ClusterRuntime;
import sqlTsinjo.config.InstanceConfig;

public class AsyncReplicatingProxy {

    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);
    private static final ExecutorService REPLICATION_POOL = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        try{
             ClusterRuntime runtime = ClusterRuntime.loadFromEnv();
        var cluster = runtime.getCluster();
        int listenPort = cluster.getLoadBalancer() == null ? 3949 : cluster.getLoadBalancer().getListenPort();

        List<InstanceConfig> masters = cluster.getMasters();
        if (masters.isEmpty()) {
            throw new IllegalStateException("No MASTER instances configured");
        }

        List<InstanceConfig> readBackends = cluster.getReadBackends();
        if (readBackends.isEmpty()) {
            readBackends = cluster.getInstances();
        }

        ExecutorService pool = Executors.newCachedThreadPool();
        try (java.net.ServerSocket server = new java.net.ServerSocket(listenPort)) {
            System.out.println("Load balancer démarré sur le port " + listenPort);
            while (true) {
                Socket client = server.accept();
                System.out.println("[LB] accepted client=" + client.getRemoteSocketAddress());
                logConnectivityForClient(client, masters);
                List<InstanceConfig> finalReadBackends = readBackends;
                pool.submit(() -> handleClient(client, masters, finalReadBackends));
            }
        }

        }catch(IOException e){
            System.out.println("Error: " + e.getMessage());
            throw e;
        }
           }

    private static void logConnectivityForClient(Socket client, List<InstanceConfig> masters) {
        String remote = String.valueOf(client.getRemoteSocketAddress());
        for (InstanceConfig m : masters) {
            boolean ok = canConnect(m.getHost(), m.getPort(), 800);
            System.out.println("[LB] connectivity client=" + remote + " -> engine=" + m.getId() + " (" + m.getHost() + ":" + m.getPort() + ") : " + (ok ? "OK" : "FAIL"));
        }
    }

    private static boolean canConnect(String host, int port, int timeoutMs) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void handleClient(Socket client, List<InstanceConfig> masters, List<InstanceConfig> readBackends) {
        Map<String, Upstream> upstreams = new ConcurrentHashMap<>();
        String[] currentDb = new String[] { null };

        try (client;
             BufferedReader clientIn = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter clientOut = new PrintWriter(
                     new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            while (true) {
                String req = readUntilSemicolon(clientIn);
                if (req == null) {
                    closeAll(upstreams);
                    return;
                }

                req = req.trim();
                if (req.isEmpty()) {
                    continue;
                }

                ParsedRequest parsed = ParsedRequest.parse(req, currentDb[0]);
                if (parsed.newCurrentDb != null) {
                    currentDb[0] = parsed.newCurrentDb;
                }

                System.out.println("[LB] req='" + req + "' kind=" + parsed.kind + " currentDb=" + currentDb[0]
                        + " partitionKey=" + parsed.partitionKey);

                InstanceConfig target;
                if (parsed.kind == RequestKind.READ) {
                    if (currentDb[0] != null && !currentDb[0].isBlank()) {
                        target = pickRendezvous(masters, "DB:" + currentDb[0]);
                    } else {
                        target = pickRoundRobin(readBackends);
                    }
                } else if (parsed.kind == RequestKind.USE) {
                    // choisir un backend (read RR) juste pour répondre
                    target = pickRoundRobin(readBackends);
                } else {
                    // WRITE: partition par objet
                    if (parsed.partitionKey == null) {
                        // fallback: écrire sur un master quelconque
                        target = masters.get(0);
                    } else {
                        target = pickRendezvous(masters, parsed.partitionKey);
                    }
                }

                System.out.println("[LB] route -> backend=" + target.getId() + " (" + target.getHost() + ":" + target.getPort() + ")");

                String response = executeOnUpstream(upstreams, target, req, currentDb[0]);
                if (response == null) {
                    clientOut.println("RESULT_MESSAGE");
                    clientOut.println("Erreur de connexion backend");
                    clientOut.println("END");
                    clientOut.flush();
                    continue;
                }
                clientOut.print(response);
                clientOut.flush();

                if (parsed.kind == RequestKind.WRITE
                        && parsed.partitionKey != null
                        && response != null
                        && !response.contains("Hadisoana")) {
                    InstanceConfig source = target;
                    String key = parsed.partitionKey;
                    REPLICATION_POOL.submit(() -> {
                        try {
                            replicateWriteToOtherMasters(source, masters, key);
                        } catch (Exception e) {
                            System.out.println("[LB][REPL] replication error: " + e.getMessage());
                        }
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("[LB] client=" + client.getRemoteSocketAddress() + " disconnected/error: " + e.getMessage());
            closeAll(upstreams);
        }
    }

    private static void replicateWriteToOtherMasters(InstanceConfig source, List<InstanceConfig> masters, String partitionKey)
            throws Exception {
        if (masters == null || masters.size() <= 1) {
            return;
        }

        String db = parseDbFromPartitionKey(partitionKey);
        if (db == null || db.isBlank()) {
            return;
        }

        ReplicationPlan plan = ReplicationPlan.fromPartitionKey(db, partitionKey);
        if (plan == null) {
            return;
        }

        Path srcRoot = Path.of(source.getDataDirectory());

        for (InstanceConfig dest : masters) {
            if (dest.getId().equals(source.getId())) continue;
            Path destRoot = Path.of(dest.getDataDirectory());
            System.out.println("[LB][REPL] " + source.getId() + " -> " + dest.getId() + " key=" + partitionKey);

            if (plan.kind == ReplicationKind.DATABASE_DIR) {
                Path srcDbDir = srcRoot.resolve(db);
                Path dstDbDir = destRoot.resolve(db);
                copyDirectory(srcDbDir, dstDbDir);
            } else {
                for (Path rel : plan.relativeFiles) {
                    copyFileIfExists(srcRoot.resolve(rel), destRoot.resolve(rel));
                }
            }
        }
    }

    private static void copyDirectory(Path srcDir, Path dstDir) throws Exception {
        if (!Files.exists(srcDir)) {
            return;
        }
        Files.walk(srcDir)
                .forEach(p -> {
                    try {
                        Path rel = srcDir.relativize(p);
                        Path target = dstDir.resolve(rel);
                        if (Files.isDirectory(p)) {
                            Files.createDirectories(target);
                        } else if (Files.isRegularFile(p)) {
                            Files.createDirectories(target.getParent());
                            Files.copy(p, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    } catch (Exception e) {
                        System.out.println("[LB][REPL] copyDirectory error: " + e.getMessage());
                    }
                });
    }

    private static void copyFileIfExists(Path srcFile, Path dstFile) throws Exception {
        if (!Files.exists(srcFile) || !Files.isRegularFile(srcFile)) {
            return;
        }
        Files.createDirectories(dstFile.getParent());
        Files.copy(srcFile, dstFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        System.out.println("[LB][REPL] copied file: " + srcFile + " -> " + dstFile);
    }

    private static String parseDbFromPartitionKey(String partitionKey) {
        // formats:
        // - DB:<db>
        // - DB:<db>|TABLE:<t>
        // - DB:<db>|VIEW:<v>
        // - DB:<db>|DOMAIN:<d>
        if (partitionKey == null) return null;
        if (!partitionKey.startsWith("DB:")) return null;
        String rest = partitionKey.substring(3);
        int sep = rest.indexOf('|');
        if (sep >= 0) return rest.substring(0, sep);
        return rest;
    }

    private enum ReplicationKind {
        DATABASE_DIR,
        FILES
    }

    private static class ReplicationPlan {
        final ReplicationKind kind;
        final List<Path> relativeFiles;

        private ReplicationPlan(ReplicationKind kind, List<Path> relativeFiles) {
            this.kind = kind;
            this.relativeFiles = relativeFiles;
        }

        static ReplicationPlan fromPartitionKey(String db, String key) {
            if (key == null) return null;
            // Database-level ops: replicate whole DB dir
            if (!key.contains("|")) {
                return new ReplicationPlan(ReplicationKind.DATABASE_DIR, List.of());
            }

            // object-level: replicate the object file + tombstone
            int idx = key.indexOf('|');
            String obj = key.substring(idx + 1);
            int colon = obj.indexOf(':');
            if (colon < 0) return null;

            String type = obj.substring(0, colon).trim().toUpperCase();
            String name = obj.substring(colon + 1).trim();
            if (name.isBlank()) return null;

            String folder;
            if (type.equals("TABLE")) folder = "tables";
            else if (type.equals("VIEW")) folder = "views";
            else if (type.equals("DOMAIN")) folder = "domains";
            else return null;

            Path relJson = Path.of(db, folder, name + ".json");
            Path relTomb = Path.of(db, folder, name + ".json.tombstone");
            return new ReplicationPlan(ReplicationKind.FILES, List.of(relJson, relTomb));
        }
    }

    private static void closeAll(Map<String, Upstream> upstreams) {
        for (Upstream u : upstreams.values()) {
            try { u.close(); } catch (Exception ignore) {}
        }
        upstreams.clear();
    }

    private static InstanceConfig pickRoundRobin(List<InstanceConfig> backends) {
        int idx = Math.floorMod(ROUND_ROBIN_INDEX.getAndIncrement(), backends.size());
        return backends.get(idx);
    }

    private static InstanceConfig pickRendezvous(List<InstanceConfig> masters, String key) {
        long bestScore = Long.MIN_VALUE;
        InstanceConfig best = masters.get(0);
        for (InstanceConfig m : masters) {
            long score = fnv1a64(key + "|" + m.getId());
            if (score > bestScore) {
                bestScore = score;
                best = m;
            }
        }
        return best;
    }

    private static long fnv1a64(String s) {
        long hash = 0xcbf29ce484222325L;
        for (int i = 0; i < s.length(); i++) {
            hash ^= s.charAt(i);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static String executeOnUpstream(Map<String, Upstream> upstreams, InstanceConfig backend, String req, String currentDb) {
        Objects.requireNonNull(backend, "backend");
        Upstream up = upstreams.computeIfAbsent(backend.getId(), ignored -> new Upstream(backend));
        try {
            up.ensureConnected();
            if (currentDb != null && !currentDb.isBlank()) {
                String useResp = up.ensureDb(currentDb);
                if (useResp != null) {
                    System.out.println("[LB] upstream " + backend.getId() + " refused USE '" + currentDb + "'");
                    return useResp;
                }
            }
            up.send(req);
            return up.readFrame();
        } catch (Exception e) {
            try { up.close(); } catch (Exception ignore) {}
            upstreams.remove(backend.getId());
            return null;
        }
    }

    private static String readUntilSemicolon(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = in.read();
            if (ch == -1) {
                return sb.length() == 0 ? null : sb.toString();
            }
            char c = (char) ch;
            if (c == ';') {
                return sb.toString();
            }
            sb.append(c);
        }
    }

    private static String readUntilEndFrame(BufferedReader in) throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if (line == null) {
                return null;
            }
            sb.append(line).append('\n');
            if (line.equals("END")) {
                return sb.toString();
            }
        }
    }

    private enum RequestKind {
        READ,
        WRITE,
        USE
    }

    private static class ParsedRequest {
        final RequestKind kind;
        final String newCurrentDb;
        final String partitionKey;

        private ParsedRequest(RequestKind kind, String newCurrentDb, String partitionKey) {
            this.kind = kind;
            this.newCurrentDb = newCurrentDb;
            this.partitionKey = partitionKey;
        }

        static ParsedRequest parse(String req, String currentDb) {
            String t = req.trim();
            String upper = t.toUpperCase();

            // USE: ampiasao <db>
            if (upper.startsWith("AMPIASAO")) {
                String db = t.substring("ampiasao".length()).trim();
                if (db.endsWith(";")) db = db.substring(0, db.length() - 1).trim();
                return new ParsedRequest(RequestKind.USE, db, null);
            }

            // heuristique READ: alaivo / asehoy
            if (upper.startsWith("ALAIVO") || upper.startsWith("ASEHOY")) {
                return new ParsedRequest(RequestKind.READ, null, null);
            }

            // CREATE DATABASE: manamboara tahiry <db>
            if (upper.startsWith("MANAMBOARA TAHIRY")) {
                String rest = t.substring("manamboara tahiry".length()).trim();
                String db = firstWord(rest);
                return new ParsedRequest(RequestKind.WRITE, null, "DB:" + db);
            }

            // DROP DATABASE: ravao ny tahiry <db>
            if (upper.startsWith("RAVAO NY TAHIRY")) {
                String rest = t.substring("ravao ny tahiry".length()).trim();
                String db = firstWord(rest);
                return new ParsedRequest(RequestKind.WRITE, null, "DB:" + db);
            }

            // CREATE TABLE: manamboara tabilao <table>
            if (upper.startsWith("MANAMBOARA TABILAO")) {
                String table = firstWord(t.substring("manamboara tabilao".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "TABLE", table));
            }

            // DROP TABLE: ravao ny tabilao <table>
            if (upper.startsWith("RAVAO NY TABILAO")) {
                String table = firstWord(t.substring("ravao ny tabilao".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "TABLE", table));
            }

            // CREATE VIEW: manamboara jery <view>
            if (upper.startsWith("MANAMBOARA JERY")) {
                String view = firstWord(t.substring("manamboara jery".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "VIEW", view));
            }

            // DROP VIEW: ravao ny jery <view>
            if (upper.startsWith("RAVAO NY JERY")) {
                String view = firstWord(t.substring("ravao ny jery".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "VIEW", view));
            }

            // CREATE DOMAIN: manamboara efitra <domain>
            if (upper.startsWith("MANAMBOARA EFITRA")) {
                String domain = firstWord(t.substring("manamboara efitra".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "DOMAIN", domain));
            }

            // DROP DOMAIN (à confirmer): ravao ny efitra <domain>
            if (upper.startsWith("RAVAO NY EFITRA")) {
                String domain = firstWord(t.substring("ravao ny efitra".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "DOMAIN", domain));
            }

            // INSERT: manampia ao@ <table>
            if (upper.startsWith("MANAMPIA")) {
                String table = parseAfterToken(t, "ao@");
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "TABLE", table));
            }

            // UPDATE: havaozy <table>
            if (upper.startsWith("HAVAOZY")) {
                String table = firstWord(t.substring("havao".length()).trim());
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "TABLE", table));
            }

            // DELETE: fafao #ao@ <table>
            if (upper.startsWith("FAFAO")) {
                String table = parseAfterToken(t, "#ao@");
                return new ParsedRequest(RequestKind.WRITE, null, objectKey(currentDb, "TABLE", table));
            }

            return new ParsedRequest(RequestKind.WRITE, null, null);
        }

        private static String objectKey(String currentDb, String type, String name) {
            if (currentDb == null || currentDb.isBlank()) return null;
            if (name == null || name.isBlank()) return null;
            return "DB:" + currentDb + "|" + type + ":" + name;
        }

        private static String firstWord(String s) {
            if (s == null) return null;
            String t = s.trim();
            if (t.isEmpty()) return null;
            int end = 0;
            while (end < t.length()) {
                char c = t.charAt(end);
                if (Character.isWhitespace(c) || c == '(' || c == ';') break;
                end++;
            }
            return t.substring(0, end).trim();
        }

        private static String parseAfterToken(String input, String token) {
            String lower = input.toLowerCase();
            int idx = lower.indexOf(token);
            if (idx < 0) return null;
            String rest = input.substring(idx + token.length()).trim();
            return firstWord(rest);
        }
    }

    private static class Upstream {
        private final InstanceConfig backend;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String currentDb;

        Upstream(InstanceConfig backend) {
            this.backend = backend;
        }

        void ensureConnected() throws IOException {
            if (socket != null && socket.isConnected() && !socket.isClosed()) return;
            socket = new Socket();
            System.out.println("[LB] connecting upstream=" + backend.getId() + " -> " + backend.getHost() + ":" + backend.getPort());
            socket.connect(new InetSocketAddress(backend.getHost(), backend.getPort()), 2000);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            currentDb = null;
        }

        String ensureDb(String db) throws IOException {
            if (db == null || db.isBlank()) return null;
            if (db.equals(currentDb)) return null;
            System.out.println("[LB] sync USE on upstream " + backend.getId() + ": ampiasao " + db);
            send("ampiasao " + db);
            String resp = readFrame();
            if (resp != null && resp.contains("Hadisoana")) {
                return resp;
            }
            currentDb = db;
            return null;
        }

        void send(String req) {
            out.print(req);
            out.print(";");
            out.flush();
        }

        String readFrame() throws IOException {
            return readUntilEndFrame(in);
        }

        void close() throws IOException {
            if (socket != null) socket.close();
        }
    }
}
