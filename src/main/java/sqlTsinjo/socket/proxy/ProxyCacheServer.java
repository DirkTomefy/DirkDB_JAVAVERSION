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

public class ProxyCacheServer {

    private static final int LISTEN_PORT = 3949;

    // Configuration des deux serveurs backend
    private static final Backend[] BACKENDS = {
        new Backend("127.0.0.1", 3948),  // Serveur 1
        new Backend("127.0.0.1", 3950)   // Serveur 2 (change le port si nécessaire)
    };

    private static final AtomicInteger ROUND_ROBIN_INDEX = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        ExecutorService pool = Executors.newCachedThreadPool();

        try (java.net.ServerSocket server = new java.net.ServerSocket(LISTEN_PORT)) {
            System.out.println("Load balancer démarré sur le port " + LISTEN_PORT);
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket client) {
        Backend backend = getNextBackend();
        if (backend == null) {
            try {
                client.close();
            } catch (IOException e) {
                // ignore
            }
            return;
        }

        try (client;
             Socket upstream = new Socket(backend.host, backend.port);
             BufferedReader clientIn = new BufferedReader(
                     new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter clientOut = new PrintWriter(
                     new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader upstreamIn = new BufferedReader(
                     new InputStreamReader(upstream.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter upstreamOut = new PrintWriter(
                     new OutputStreamWriter(upstream.getOutputStream(), StandardCharsets.UTF_8), true)) {

            System.out.println("Connexion client -> " + backend);

            while (true) {
                String req = readUntilSemicolon(clientIn);
                if (req == null) {
                    return;
                }

                req = req.trim();
                if (req.isEmpty()) {
                    continue;
                }

                // Transférer la requête vers le backend
                upstreamOut.print(req);
                upstreamOut.print(";");
                upstreamOut.flush();

                // Lire la réponse du backend et la renvoyer au client
                String response = readUntilEndFrame(upstreamIn);
                if (response == null) {
                    return;
                }

                clientOut.print(response);
                clientOut.flush();
            }
        } catch (IOException e) {
            System.err.println("Erreur de connexion avec " + backend + ": " + e.getMessage());
        }
    }

    private static Backend getNextBackend() {
        int index = ROUND_ROBIN_INDEX.getAndIncrement() % BACKENDS.length;
        return BACKENDS[Math.abs(index)];
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
