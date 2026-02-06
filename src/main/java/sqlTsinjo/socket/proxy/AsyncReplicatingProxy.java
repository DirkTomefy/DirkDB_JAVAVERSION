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
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncReplicatingProxy {

    private static final int LISTEN_PORT = 3949;

    private static final Backend[] BACKENDS = {
        new Backend("127.0.0.1", 3948),  // Primaire
        new Backend("127.0.0.1", 3950)   // Secondaire
    };

    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);
    private static final ExecutorService REPLICATION_POOL = Executors.newCachedThreadPool();

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (java.net.ServerSocket server = new java.net.ServerSocket(LISTEN_PORT)) {
            System.out.println("Async replicating proxy démarré sur le port " + LISTEN_PORT);
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket client) {
        Backend primary = BACKENDS[0];
        Backend secondary = BACKENDS[1];

        try (client;
             BufferedReader clientIn = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter clientOut = new PrintWriter(
                     new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            while (true) {
                String req = readUntilSemicolon(clientIn);
                if (req == null) {
                    return;
                }

                req = req.trim();
                if (req.isEmpty()) {
                    continue;
                }

                boolean isWrite = isWriteOperation(req);

                if (isWrite) {
                    // Écrire sur le primaire et répondre immédiatement
                    String response = executeOnServer(req, primary);
                    if (response != null) {
                        clientOut.print(response);
                        clientOut.flush();
                        
                        // Réplication asynchrone en arrière-plan
                        REPLICATION_POOL.submit(() -> {
                            try {
                                executeOnServer(req, secondary);
                            } catch (Exception e) {
                                System.err.println("Erreur réplication async: " + e.getMessage());
                            }
                        });
                    } else {
                        clientOut.println("RESULT_MESSAGE");
                        clientOut.println("Erreur écriture primaire");
                        clientOut.println("END");
                        clientOut.flush();
                    }
                } else {
                    // Lecture : round-robin
                    Backend readBackend = getNextReadBackend();
                    String response = executeRead(req, readBackend);
                    if (response != null) {
                        clientOut.print(response);
                        clientOut.flush();
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur de connexion client: " + e.getMessage());
        }
    }

    private static String executeRead(String req, Backend backend) {
        return executeOnServer(req, backend);
    }

    private static String executeOnServer(String req, Backend backend) {
        try (Socket socket = new Socket(backend.host, backend.port);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

            out.print(req);
            out.print(";");
            out.flush();

            return readUntilEndFrame(in);
        } catch (IOException e) {
            System.err.println("Erreur connexion à " + backend + ": " + e.getMessage());
            return null;
        }
    }

    private static Backend getNextReadBackend() {
        int index = ROUND_ROBIN_INDEX.getAndIncrement() % BACKENDS.length;
        return BACKENDS[Math.abs(index)];
    }

    private static boolean isWriteOperation(String req) {
        String normalized = req.trim().toUpperCase();
        return normalized.startsWith("MANAMPIA")      // INSERT
            || normalized.startsWith("AMPIDITRA")     // UPDATE
            || normalized.startsWith("DELETE")
            || normalized.startsWith("CREATE")
            || normalized.startsWith("DROP");
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

    private static class Backend {
        final String host;
        final int port;

        Backend(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String toString() {
            return host + ":" + port;
        }
    }
}
