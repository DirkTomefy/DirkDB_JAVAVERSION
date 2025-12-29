package sqlTsinjo.cli;

import java.io.IOException;
import java.util.Scanner;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.main.GeneralRqstAsker;

public class DirkDbCli {

    public static void main(String[] args) {
        AppContext context = new AppContext(null, "Tomefy", true);
        printCopyright();

        // On utilise un seul Scanner pour toute la durée de l'application
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("|DirkDB > ");
                String request = scanner.nextLine();

                // Permettre de quitter proprement la boucle
                if (request.equalsIgnoreCase("exit") || request.equalsIgnoreCase("quit")) {
                    System.out.println("Goodbye!");
                    break;
                }

                if (request.trim().isEmpty())
                    continue;

                try {
                    GeneralRqstAsker.askRequest(request, context);
                } catch (ParseNomException | EvalErr | IOException e) {
                    if (context.debugMode) {
                        e.printStackTrace();
                    } else {
                        System.err.println("Error: " + e.getMessage());

                    }
                }
            }
        } // Le scanner se ferme automatiquement ici à la fin du programme
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