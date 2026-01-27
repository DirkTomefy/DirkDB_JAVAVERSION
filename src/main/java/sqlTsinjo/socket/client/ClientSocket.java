package sqlTsinjo.socket.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.result.RequestResult;

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

                    RequestResult response = readResponse(in);
                    System.out.println(response);

                    String msg = response.getMessage();
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

    private static RequestResult readResponse(BufferedReader in) throws IOException {
        Relation relation = null;

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
                    JsonNode node = MAPPER.readTree(json);

                    if (relation == null) {
                        relation = new Relation();

                        JsonNode nameNode = node.get("name");
                        String relationName = nameNode == null || nameNode.isNull() ? null : nameNode.asText();
                        relation.setName(relationName);

                        Vector<QualifiedIdentifier> fieldNames = new Vector<>();
                        Vector<Domain> domains = new Vector<>();

                        JsonNode cols = node.get("columns");
                        if (cols != null && cols.isArray()) {
                            for (JsonNode col : cols) {
                                String colName = "";
                                String origin = null;

                                JsonNode n = col.get("name");
                                if (n != null && !n.isNull()) {
                                    colName = n.asText();
                                }

                                JsonNode o = col.get("origin");
                                if (o != null && !o.isNull()) {
                                    origin = o.asText();
                                }

                                fieldNames.add(new QualifiedIdentifier(origin, colName));
                                domains.add(Domain.makeUniversalDomain());
                            }
                        }

                        relation.setFieldName(fieldNames);
                        relation.setDomaines(domains);
                        relation.setIndividus(new Vector<>());
                    }

                    JsonNode rows = node.get("rows");
                    if (rows != null && rows.isArray()) {
                        Vector<Vector<Object>> individus = relation.getIndividus();
                        for (JsonNode row : rows) {
                            Vector<Object> ind = new Vector<>();
                            if (row != null && row.isArray()) {
                                for (JsonNode cell : row) {
                                    ind.add(jsonNodeToValue(cell));
                                }
                            }
                            individus.add(ind);
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
            return RequestResult.withRelation(relation, false);
        }

        return RequestResult.withMessage(messagePart.toString());
    }

    private static Object jsonNodeToValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isInt()) {
            return node.intValue();
        }
        if (node.isLong()) {
            return node.longValue();
        }
        if (node.isFloatingPointNumber()) {
            return node.doubleValue();
        }
        if (node.isBoolean()) {
            return node.booleanValue();
        }
        return node.toString();
    }
}
