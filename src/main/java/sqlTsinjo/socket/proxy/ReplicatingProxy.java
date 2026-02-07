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

public class ReplicatingProxy {

    private static final int LISTEN_PORT = 3949;

    // Configuration des deux serveurs backend
    private static final Backend[] BACKENDS = {
        new Backend("127.0.0.1", 3948),  // Serveur primaire
        new Backend("127.0.0.1", 3950)   // Serveur secondaire
    };

    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (java.net.ServerSocket server = new java.net.ServerSocket(LISTEN_PORT)) {
            System.out.println("Replicating proxy démarré sur le port " + LISTEN_PORT);
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket client) {
        Backend primary = BACKENDS[0];  // Primaire pour les écritures
        Backend secondary = BACKENDS[1];  // Secondaire pour réplication

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
                    // Réplication synchrone : écrire sur les 2 serveurs
                    String response = replicateWrite(req, primary, secondary);
                    if (response != null) {
                        clientOut.print(response);
                        clientOut.flush();
                    } else {
                        clientOut.println("RESULT_MESSAGE");
                        clientOut.println("Erreur de réplication");
                        clientOut.println("END");
                        clientOut.flush();
                    }
                } else {
                    // Lecture : round-robin entre les serveurs
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

    private static String replicateWrite(String req, Backend primary, Backend secondary) {
        try {
            // Écrire sur le primaire
            String primaryResponse = executeOnServer(req, primary);
            if (primaryResponse == null) {
                return null;
            }

            // Écrire sur le secondaire
            String secondaryResponse = executeOnServer(req, secondary);
            if (secondaryResponse == null) {
                System.err.println("Erreur réplication sur " + secondary);
                // On continue quand même, le primaire a réussi
            }

            return primaryResponse;  // Retourner la réponse du primaire
        } catch (Exception e) {
            System.err.println("Erreur lors de la réplication: " + e.getMessage());
            return null;
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
