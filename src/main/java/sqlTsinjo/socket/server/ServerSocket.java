package sqlTsinjo.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Relation;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.dispatch.GeneralRqstAsker;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.result.RequestResult;

public class ServerSocket {

    private static final int DEFAULT_PORT = 3948;
    private static final int PAGE_SIZE_ROWS = 20;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        ExecutorService pool = Executors.newCachedThreadPool();

        try (java.net.ServerSocket server = new java.net.ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                pool.submit(() -> handleClient(client));
            }
        }
    }

    private static void handleClient(Socket client) {
        try (client;
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {

            AppContext ctx = new AppContext(null, "remote", false);

            while (true) {
                String request = readUntilSemicolon(in);
                if (request == null) {
                    return;
                }

                request = request.trim();
                if (request.isEmpty()) {
                    continue;
                }

                try {
                    RequestResult result = GeneralRqstAsker.askRequest(request, ctx);
                    writeResult(out, result);
                } catch (Exception e) {
                    String msg = ctx.isDebugMode() ? (e.toString()) : e.getMessage();
                    out.println("RESULT_MESSAGE");
                    out.println("Hadisoana : " + msg);
                    out.println("END");
                }
            }
        } catch (IOException e) {
            // client disconnected or IO issue
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

    private static void writeResult(PrintWriter out, RequestResult result) {
        Optional<Relation> relOpt = result.getRelation();
        if (relOpt.isPresent()) {
            Relation rel = relOpt.get();
            Vector<Vector<Object>> rows = rel.getIndividus();
            int totalPages = (rows.size() + PAGE_SIZE_ROWS - 1) / PAGE_SIZE_ROWS;
            if (totalPages == 0) totalPages = 1;

            for (int page = 0; page < totalPages; page++) {
                int from = page * PAGE_SIZE_ROWS;
                int to = Math.min(from + PAGE_SIZE_ROWS, rows.size());
                String json = relationPageToJson(rel, page + 1, totalPages, from, to);
                out.println("RELATION_JSON_PAGE " + (page + 1) + "/" + totalPages);
                out.println(json);
            }
            out.println("RELATION_JSON_END");

            out.println("RESULT_MESSAGE");
            out.println("OK");
            out.println("END");
            return;
        }

        out.println("RESULT_MESSAGE");
        out.println(result.getMessage() == null ? "" : result.getMessage());
        out.println("END");
    }

    private static String relationPageToJson(Relation rel, int page, int totalPages, int fromInclusive, int toExclusive) {
        Vector<QualifiedIdentifier> fns = rel.getFieldName();
        ColumnDto[] columns = new ColumnDto[fns.size()];
        for (int i = 0; i < fns.size(); i++) {
            QualifiedIdentifier q = fns.get(i);
            columns[i] = new ColumnDto(q.getName(), q.getOrigin());
        }

        Vector<Vector<Object>> individuals = rel.getIndividus();
        int size = Math.max(0, toExclusive - fromInclusive);
        Object[][] rows = new Object[size][];
        for (int r = 0; r < size; r++) {
            Vector<Object> row = individuals.get(fromInclusive + r);
            Object[] arr = new Object[row.size()];
            for (int c = 0; c < row.size(); c++) {
                Object v = row.get(c);
                if (v instanceof char[] ca) {
                    arr[c] = new String(ca);
                } else {
                    arr[c] = v;
                }
            }
            rows[r] = arr;
        }

        RelationPageDto dto = new RelationPageDto(rel.getName(), page, totalPages, columns, rows);
        try {
            return MAPPER.writeValueAsString(dto);
        } catch (Exception e) {
            return "{\"type\":\"relation\",\"error\":" + String.valueOf(e.getMessage()) + "}";
        }
    }
}
