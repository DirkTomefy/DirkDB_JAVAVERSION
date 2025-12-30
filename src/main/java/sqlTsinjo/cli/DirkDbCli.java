package sqlTsinjo.cli;

import java.io.IOException;
import java.util.Scanner;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.main.GeneralRqstAsker;

public class DirkDbCli {

    public static void main(String[] args) {
        AppContext context = new AppContext(null, "Tomefy", false);
        printCopyright();

        try (Scanner scanner = new Scanner(System.in)) {
            StringBuilder requestBuilder = new StringBuilder();

            while (true) {
                // Afficher le prompt seulement si on commence une nouvelle requête
                if (requestBuilder.length() == 0) {
                    System.out.print("|DirkDB > ");
                } else {
                    System.out.print("        > "); // Indentation pour les lignes suivantes
                }

                String line = scanner.nextLine();

                // Commandes de contrôle
                if (requestBuilder.length() == 0) {
                    if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                        System.out.println("Goodbye!");
                        break;
                    }
                    if (line.equalsIgnoreCase("clear")) {
                        System.out.print("\033[H\033[2J"); // Clear console
                        System.out.flush();
                        continue;
                    }
                    if (line.equalsIgnoreCase("help")) {
                        showHelp();
                        continue;
                    }
                }

                // Ajouter la ligne (sauf si c'est une commande d'annulation)
                if (line.equalsIgnoreCase("\\c") || line.equalsIgnoreCase("cancel")) {
                    System.out.println("Query cancelled.");
                    requestBuilder.setLength(0);
                    continue;
                }

                // Ajouter la ligne à la requête
                requestBuilder.append(line).append(" ");

                // Vérifier si la requête est terminée (se termine par un point-virgule)
                String currentRequest = requestBuilder.toString().trim();
                if (currentRequest.endsWith(";")) {
                    // Retirer le point-virgule final
                    String fullRequest = currentRequest.substring(0, currentRequest.length() - 1).trim();

                    if (!fullRequest.isEmpty()) {
                        try {
                            GeneralRqstAsker.askRequest(fullRequest, context);
                        } catch (ParseNomException | EvalErr | IOException e) {
                            if (context.debugMode) {
                                e.printStackTrace();
                            } else {
                                System.err.println("Error: " + e.getMessage());
                            }
                        }
                    }

                    // Réinitialiser pour la prochaine requête
                    requestBuilder.setLength(0);
                }
            }
        }
    }

    private static void showHelp() {
        System.out.println("\nCommands:");
        System.out.println("  exit, quit  - Exit the program");
        System.out.println("  clear       - Clear the screen");
        System.out.println("  help        - Show this help");
        System.out.println("  \\c, cancel  - Cancel current multiline query");
        System.out.println("\nEnter SQL statements ending with ; for multiline queries.");
        System.out.println();
    }

    private static void printCopyright() {
        System.out.println("========================================");
        System.out.println(" DirkDB SQL Engine - Command Line Tool");
        System.out.println(" Version 1.0.0");
        System.out.println();
        System.out.println(" Copyright (c) 2025");
        System.out.println(" Author : Dirk Tomefy");
        System.out.println(" All rights reserved.");
        System.out.println();
        System.out.println(" Type 'exit' or 'quit' to leave.");
        System.out.println("========================================");
    }
}