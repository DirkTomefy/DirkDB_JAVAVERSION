package cli;

import java.util.Scanner;

public class DirkDbCli {

    public static void main(String[] args) {
        printCopyright();
        while (true) {
            askRequest();
        }
    }

    private static void askRequest(){
        Scanner scanner = new Scanner(System.in);
        System.out.print("|DirkDB > ");
        String _=scanner.nextLine();
        // scanner.close();
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
        System.out.println(" DirkDB is a lightweight educational SQL engine.");
        System.out.println("========================================");
    }
}
