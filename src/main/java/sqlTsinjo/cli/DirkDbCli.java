package sqlTsinjo.cli;

import java.util.Scanner;

import sqlTsinjo.query.dispatch.GeneralRqstAsker;
import sqlTsinjo.query.result.RequestResult;

public class DirkDbCli {

    public static void main(String[] args)  {
        AppContext context = new AppContext(null, "Herman", false);
        printCopyright();

        try (Scanner scanner = new Scanner(System.in)) {
            StringBuilder requestBuilder = new StringBuilder();

            while (true) {
                // Asehoy ny prompt raha vao manomboka fangatahana vaovao
                if (requestBuilder.length() == 0) {
                    System.out.print("[My Database] : ");
                } else {
                    System.out.print("        : "); // Fanalavirana ho an'ny andalana manaraka
                }

                String line = scanner.nextLine();

                // Baiko fanaraha-maso
                if (requestBuilder.length() == 0) {
                    if (line.equalsIgnoreCase("miala") || line.equalsIgnoreCase("mivaoka")) {
                        System.out.println("Veloma ary!");
                        break;
                    }
                    if (line.equalsIgnoreCase("diovy") || line.equalsIgnoreCase("!")) {
                        System.out.print("\033[H\033[2J"); // Mamafa ny efijery
                        System.out.flush();
                        continue;
                    }
                    if (line.equalsIgnoreCase("vonjy") || line.equalsIgnoreCase("?")) {
                        showHelp();
                        continue;
                    }
                }

                // Fanafoanana fangatahana
                if (line.equalsIgnoreCase("\\c") || line.equalsIgnoreCase("cancel")) {
                    System.out.println("Nofoanana ny fangatahana.");
                    requestBuilder.setLength(0);
                    continue;
                }

                // Ampiana ao amin'ny fangatahana
                requestBuilder.append(line).append(" ");

                // Hijery raha vita ny fangatahana (mifarana amin'ny ;)
                String currentRequest = requestBuilder.toString().trim();
                if (currentRequest.endsWith(";")) {
                    // Esorina ny teboka famaranana
                    String fullRequest = currentRequest
                            .substring(0, currentRequest.length() - 1)
                            .trim();

                    if (!fullRequest.isEmpty()) {
                        try {
                            RequestResult result = GeneralRqstAsker.askRequest(fullRequest, context);
                            System.out.println(result);
                        } catch (Exception e) {
                            if (context.debugMode) {
                                e.printStackTrace();
                            } else {
                                System.err.println("Hadisoana : " + e.getMessage());
                            }
                        }
                    }

                    // Averina ho aotra ho an'ny fangatahana manaraka
                    requestBuilder.setLength(0);
                }
            }
        }
    }

    private static void showHelp() {
        System.out.println("\nBaiko misy:");
        System.out.println("  miala, mivaoka  - Hiala amin'ny programa");
        System.out.println("  diovy       - Mamafa ny efijery");
        System.out.println("  vonjy        - Mampiseho ity fanazavana ity");
        System.out.println("  \\c, cancel  - Manafoana ny fangatahana maromaro andalana");
        System.out.println("\nAmpidiro ny baiko SQL mifarana amin'ny ; raha andalana maromaro.");
        System.out.println();
    }

    private static void printCopyright() {
        System.out.println("========================================");
        System.out.println(" Dika 1.0.0");
        System.out.println();
        System.out.println(" Zo rehetra voatokana © 2025");
        System.out.println(" Voaaro avokoa ny zo rehetra.");
        System.out.println();
        System.out.println("========================================");
    }
}
