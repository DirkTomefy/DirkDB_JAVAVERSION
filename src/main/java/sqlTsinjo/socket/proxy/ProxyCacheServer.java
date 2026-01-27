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

public class ProxyCacheServer {

    private static final int LISTEN_PORT = 3949;
    private static final int UPSTREAM_PORT = 3948;
    private static final String UPSTREAM_HOST = "127.0.0.1";

    private static final long TTL_MILLIS = 30_000;
    private static final int MAX_ENTRIES = 200;

    private static final Pattern USE_DB_REQUEST = Pattern.compile("^AMPIASAO\\s+(.+?)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern USE_DB_SUCCESS = Pattern
            .compile("^Ny tahiry\\s*:\\s*(.+?)\\s+dia\\s+miasa\\s+ankehitriny\\s*$");

    private static final ProxyCache CACHE = new ProxyCache(TTL_MILLIS, MAX_ENTRIES);

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (java.net.ServerSocket server = new java.net.ServerSocket(LISTEN_PORT)) {
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket client) {
        try (client;
                Socket upstream = new Socket(UPSTREAM_HOST, UPSTREAM_PORT);
                BufferedReader clientIn = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter clientOut = new PrintWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader upstreamIn = new BufferedReader(
                        new InputStreamReader(upstream.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter upstreamOut = new PrintWriter(
                        new OutputStreamWriter(upstream.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String currentDb = null;

            while (true) {
                String req = readUntilSemicolon(clientIn);
                if (req == null) {
                    return;
                }

                req = req.trim();
                if (req.isEmpty()) {
                    continue;
                }

                String normalized = normalize(req);

                boolean isUseDb = isUseDb(normalized);
                boolean isSelect = isSelect(normalized);
                boolean isWrite = isWrite(normalized);

                String cacheKey = null;
                if (isSelect) {
                    cacheKey = ((currentDb == null) ? "" : currentDb) + "|" + normalized;
                    String cached = CACHE.getIfPresent(cacheKey);
                    if (cached != null) {
                        clientOut.print(cached);
                        clientOut.flush();
                        continue;
                    }
                }

                upstreamOut.print(normalized);
                upstreamOut.print(";");
                upstreamOut.flush();

                String responsePayload = readUntilEndFrame(upstreamIn);
                if (responsePayload == null) {
                    return;
                }

                if (isUseDb) {
                    String maybeDb = extractDbFromUseDbSuccess(responsePayload);
                    if (maybeDb != null) {
                        currentDb = maybeDb;
                    }
                }

                if (isWrite) {
                    if (currentDb != null) {
                        CACHE.clearByDbPrefix(currentDb);
                    } else {
                        CACHE.clearAll();
                    }
                }

                if (isSelect && cacheKey != null) {
                    CACHE.put(cacheKey, responsePayload);
                }

                clientOut.print(responsePayload);
                clientOut.flush();
            }
        } catch (IOException e) {
            // disconnected
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

    private static boolean isUseDb(String normalized) {
        return USE_DB_REQUEST.matcher(normalized).matches();
    }

    private static boolean isSelect(String normalized) {
        return startsWithNoCase(normalized, "ALAIVO") || startsWithNoCase(normalized, "SELECT");
    }

    private static boolean isWrite(String normalized) {
        return startsWithNoCase(normalized, "MANAMPIA")
                || startsWithNoCase(normalized, "AMPIDITRA")
                || startsWithNoCase(normalized, "UPDATE")
                || startsWithNoCase(normalized, "DELETE")
                || startsWithNoCase(normalized, "CREATE")
                || startsWithNoCase(normalized, "DROP");
    }

    private static boolean startsWithNoCase(String s, String prefix) {
        if (s.length() < prefix.length()) {
            return false;
        }
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private static String normalize(String req) {
        return req.trim().replaceAll("\\s+", " ");
    }

    private static String extractDbFromUseDbSuccess(String responsePayload) {
        String[] lines = responsePayload.split("\\R");
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            Matcher m = USE_DB_SUCCESS.matcher(line.trim());
            if (m.matches()) {
                return m.group(1).trim();
            }
        }
        return null;
    }
}
