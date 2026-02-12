package sqlTsinjo.socket.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.protocol.QueryResponseDto;
import sqlTsinjo.protocol.RelationDto;
import sqlTsinjo.protocol.RelationPageDto;

public class ClientSocket {

    private static final int DEFAULT_PORT = 3949;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final Pattern USE_DB_SUCCESS = Pattern
            .compile("^Ny tahiry\\s*:\\s*(.+?)\\s+dia\\s+miasa\\s+ankehitriny\\s*$");

    public static void main(String[] args) throws IOException {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;

        try (Socket socket = new Socket(host, port);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                Scanner scanner = new Scanner(System.in)) {

            StringBuilder requestBuilder = new StringBuilder();
            String currentDatabaseName = null;
            while (true) {
                if (requestBuilder.length() == 0) {
                    String promptDb = (currentDatabaseName == null || currentDatabaseName.isBlank())
                            ? "DirkDB"
                            : currentDatabaseName;
                    System.out.print("|" + promptDb + "> ");
                } else {
                    System.out.print("        > ");
                }

                String line = scanner.nextLine();
                if (requestBuilder.length() == 0) {
                    if (line.equalsIgnoreCase("miala") || line.equalsIgnoreCase("mivaoka")) {
                        break;
                    }

                    if (line.equalsIgnoreCase("diovy") || line.equalsIgnoreCase("!")) {
                        System.out.print("\033[H\033[2J");
                        System.out.flush();
                        continue;
                    }
                }

                requestBuilder.append(line).append(" ");
                String current = requestBuilder.toString().trim();
                if (current.endsWith(";")) {
                    String full = current.substring(0, current.length() - 1).trim();
                    requestBuilder.setLength(0);
                    if (full.isEmpty()) {
                        continue;
                    }

                    out.print(full);
                    out.print(";");
                    out.flush();

                    QueryResponseDto response = readResponse(in);
                    printResponse(response);

                    String msg = response.message;
                    if (msg != null) {
                        Matcher m = USE_DB_SUCCESS.matcher(msg.trim());
                        if (m.matches()) {
                            currentDatabaseName = m.group(1).trim();
                        }
                    }
                }
            }
        }
    }

    private static QueryResponseDto readResponse(BufferedReader in) throws IOException {
        RelationDto relation = null;
        StringBuilder messagePart = new StringBuilder();

        while (true) {
            String line = in.readLine();
            if (line == null) {
                break;
            }
            if (line.equals("END")) {
                break;
            }

            if (line.startsWith("RELATION_JSON_PAGE")) {
                String json = in.readLine();
                if (json == null) {
                    break;
                }
                try {
                    RelationPageDto page = MAPPER.readValue(json, RelationPageDto.class);
                    if (page != null) {
                        if (relation == null) {
                            relation = new RelationDto();
                            relation.name = page.name;
                            if (page.columns != null) {
                                for (var c : page.columns) {
                                    relation.columns.add(c);
                                }
                            }
                        }

                        if (page.rows != null) {
                            for (Object[] row : page.rows) {
                                java.util.ArrayList<Object> r = new java.util.ArrayList<>();
                                if (row != null) {
                                    for (Object cell : row) {
                                        r.add(cell);
                                    }
                                }
                                relation.rows.add(r);
                            }
                        }
                    }
                } catch (Exception e) {
                    // ignore malformed page
                }
                continue;
            }

            if (line.equals("RELATION_JSON_END")) {
                continue;
            }

            if (line.equals("RESULT_MESSAGE")) {
                String msg;
                while ((msg = in.readLine()) != null) {
                    if (msg.equals("END")) {
                        line = "END";
                        break;
                    }
                    if (!msg.isEmpty()) {
                        if (messagePart.length() > 0) {
                            messagePart.append('\n');
                        }
                        messagePart.append(msg);
                    }
                }
                break;
            }

            if (!line.isEmpty()) {
                if (messagePart.length() > 0) {
                    messagePart.append('\n');
                }
                messagePart.append(line);
            }
        }

        if (relation != null) {
            return QueryResponseDto.withRelation(relation, messagePart.toString());
        }

        return QueryResponseDto.withMessage(messagePart.toString());
    }

    private static void printResponse(QueryResponseDto response) {
        if (response == null) {
            System.out.println("<null>");
            return;
        }

        if (response.relation != null) {
            RelationDto rel = response.relation;
            String name = rel.name == null ? "" : rel.name;
            System.out.println("Relation: " + name);

            if (rel.columns != null && !rel.columns.isEmpty()) {
                StringBuilder header = new StringBuilder();
                for (int i = 0; i < rel.columns.size(); i++) {
                    var c = rel.columns.get(i);
                    String colName = c == null || c.name == null ? "" : c.name;
                    if (i > 0) header.append(" | ");
                    header.append(colName);
                }
                System.out.println(header);
            }

            if (rel.rows != null) {
                for (var row : rel.rows) {
                    StringBuilder sb = new StringBuilder();
                    if (row != null) {
                        for (int i = 0; i < row.size(); i++) {
                            if (i > 0) sb.append(" | ");
                            Object v = row.get(i);
                            sb.append(v == null ? "null" : String.valueOf(v));
                        }
                    }
                    System.out.println(sb);
                }
            }
        }

        if (response.message != null && !response.message.isBlank()) {
            System.out.println(response.message);
        }
    }
}
