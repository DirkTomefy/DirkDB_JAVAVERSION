package sqlTsinjo.socket.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalProxyCacheServer {

    private static final Pattern USE_DB = Pattern.compile("^\\s*AMPIASAO\\s+(.+?)\\s*$", Pattern.CASE_INSENSITIVE);

    public static void main(String[] args) throws IOException {
        String upstreamHost = args.length > 0 ? args[0] : "127.0.0.1";
        int upstreamPort = args.length > 1 ? Integer.parseInt(args[1]) : 3949;
        int listenPort = args.length > 2 ? Integer.parseInt(args[2]) : 3951;

        long ttlMillis = args.length > 3 ? Long.parseLong(args[3]) : 3000L;
        int maxEntries = args.length > 4 ? Integer.parseInt(args[4]) : 200;

        ProxyCache cache = new ProxyCache(ttlMillis, maxEntries);

        ExecutorService pool = Executors.newCachedThreadPool();
        try (java.net.ServerSocket server = new java.net.ServerSocket(listenPort)) {
            System.out.println("LocalProxyCacheServer listening on :" + listenPort + " -> " + upstreamHost + ":" + upstreamPort
                    + " ttlMillis=" + ttlMillis + " maxEntries=" + maxEntries);
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client, upstreamHost, upstreamPort, cache));
            }
        }
    }

    private static void handleClient(Socket client, String upstreamHost, int upstreamPort, ProxyCache cache) {
        String[] currentDb = new String[] { null };

        try (client;
             Socket upstream = new Socket(upstreamHost, upstreamPort);
             BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter clientOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader upstreamIn = new BufferedReader(new InputStreamReader(upstream.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter upstreamOut = new PrintWriter(new OutputStreamWriter(upstream.getOutputStream(), StandardCharsets.UTF_8), true)) {

            while (true) {
                String req = readUntilSemicolon(clientIn);
                if (req == null) {
                    return;
                }

                req = req.trim();
                if (req.isEmpty()) {
                    continue;
                }

                Kind kind = classify(req);

                if (kind == Kind.USE) {
                    Matcher m = USE_DB.matcher(req);
                    if (m.matches()) {
                        currentDb[0] = m.group(1).trim();
                    } else {
                        currentDb[0] = null;
                    }
                    cache.clearAll();
                } else if (kind == Kind.WRITE) {
                    cache.clearAll();
                }

                if (kind == Kind.READ) {
                    String key = (currentDb[0] == null ? "" : currentDb[0]) + "|" + req;
                    String cached = cache.getIfPresent(key);
                    if (cached != null) {
                        clientOut.print(cached);
                        clientOut.flush();
                        continue;
                    }

                    String resp = forward(upstreamOut, upstreamIn, req);
                    if (resp == null) {
                        return;
                    }
                    cache.put(key, resp);
                    clientOut.print(resp);
                    clientOut.flush();
                    continue;
                }

                String resp = forward(upstreamOut, upstreamIn, req);
                if (resp == null) {
                    return;
                }
                clientOut.print(resp);
                clientOut.flush();

            }
        } catch (IOException e) {
            // client disconnected
        }
    }

    private enum Kind { READ, WRITE, USE }

    private static Kind classify(String req) {
        String u = req.trim().toUpperCase();
        if (u.startsWith("AMPIASAO")) return Kind.USE;
        if (u.startsWith("ALAIVO") || u.startsWith("ASEHOY")) return Kind.READ;
        return Kind.WRITE;
    }

    private static String forward(PrintWriter upstreamOut, BufferedReader upstreamIn, String req) throws IOException {
        upstreamOut.print(req);
        upstreamOut.print(";");
        upstreamOut.flush();
        return readUntilEndFrame(upstreamIn);
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
}
