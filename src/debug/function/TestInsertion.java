package debug.function;

import java.util.Arrays;
import java.util.Vector;

import base.Domain;
import base.Relation;
import base.domains.CHAR;
import base.domains.VARCHAR;
import base.err.DomainOutOfBonds;
import base.err.DomainSupportErr;

public class TestInsertion {

    // public static void makeTestCharVsVarChar() throws DomainOutOfBonds {

    //     // =========================================================
    //     // 1. Définition structure : VARCHAR(3) et CHAR(3)
    //     // =========================================================
    //     Vector<String> fields = new Vector<>();
    //     fields.add("v"); // VARCHAR
    //     fields.add("c"); // CHAR

    //     Vector<Domain> domains = new Vector<>();
    //     domains.add(new VARCHAR(3).intoDomain()); // accepte uniquement String
    //     domains.add(new CHAR(3).intoDomain()); // accepte uniquement char[]

    //     Relation rel = new Relation("relTestTypes", fields, domains, new Vector<>());

    //     System.out.println("===== TEST VARCHAR vs CHAR =====");

    //     // =========================================================
    //     // 2. Individus de test
    //     // =========================================================

    //     // ✔ Correct: VARCHAR = String, CHAR = char[]
    //     Vector<Object> indOK = new Vector<>(Arrays.asList(
    //             "abc", // VARCHAR OK
    //             new char[] { 'x', 'y', 'z' } // CHAR OK
    //     ));

    //     // ❌ VARCHAR incorrect = char[]
    //     Vector<Object> indVarcharBad = new Vector<>(Arrays.asList(
    //             new char[] { 'e', 'r', 'r' }, // mauvais type
    //             new char[] { 'x', 'y', 'z' }));

    //     // ❌ CHAR incorrect = String
    //     Vector<Object> indCharBad = new Vector<>(Arrays.asList(
    //             "ok", // VARCHAR OK
    //             "bad" // mauvais type
    //     ));

    //     // ❌ VARCHAR incorrect autre type
    //     Vector<Object> indVarcharTypeErr = new Vector<>(Arrays.asList(
    //             Integer.valueOf(12), // pas String
    //             new char[] { 'a', 'b', 'c' }));

    //     // ❌ CHAR incorrect autre type
    //     Vector<Object> indCharTypeErr = new Vector<>(Arrays.asList(
    //             "yo",
    //             Integer.valueOf(10) // pas char[]
    //     ));

    //     // =========================================================
    //     // 3. TESTS
    //     // =========================================================

    //     // ---> Test OK
    //     try {
    //         System.out.println("Test 1 : insertion valide (String + char[])...");
    //         rel.insertNewInd(indOK);
    //         System.out.println("OK : " + rel + "\n");
    //     } catch (Exception e) {
    //         System.out.println("ÉCHEC (aurait dû réussir) : " + e.getMessage());
    //     }

    //     // ---> VARCHAR doit refuser char[]
    //     try {
    //         System.out.println("Test 2 : VARCHAR reçoit un char[] (doit échouer)...");
    //         rel.insertNewInd(indVarcharBad);
    //         System.out.println("ERREUR → ne devait PAS réussir ?");
    //     } catch (DomainSupportErr e) {
    //         System.out.println("OK (DomainSupportErr attendu) : " + e.getMessage() + "\n");
    //     }

    //     // ---> CHAR doit refuser String
    //     try {
    //         System.out.println("Test 3 : CHAR reçoit un String (doit échouer)...");
    //         rel.insertNewInd(indCharBad);
    //         System.out.println("ERREUR → ne devait PAS réussir !");
    //     } catch (DomainSupportErr e) {
    //         System.out.println("OK (DomainSupportErr attendu) : " + e.getMessage() + "\n");
    //     }

    //     // ---> VARCHAR reçoit un type bizarre
    //     try {
    //         System.out.println("Test 4 : VARCHAR reçoit Integer (doit échouer)...");
    //         rel.insertNewInd(indVarcharTypeErr);
    //         System.out.println("ERREUR → ne devait PAS réussir !");
    //     } catch (DomainSupportErr e) {
    //         System.out.println("OK (DomainSupportErr attendu) : " + e.getMessage() + "\n");
    //     }

    //     // ---> CHAR reçoit un type bizarre
    //     try {
    //         System.out.println("Test 5 : CHAR reçoit Integer (doit échouer)...");
    //         rel.insertNewInd(indCharTypeErr);
    //         System.out.println("ERREUR → ne devait PAS réussir !");
    //     } catch (DomainSupportErr e) {
    //         System.out.println("OK (DomainSupportErr attendu) : " + e.getMessage() + "\n");
    //     }

    //     System.out.println("===== FIN TEST =====");
    // }

    // public static void makeGeneralTest() {

    //     // =====================
    //     // 1. Définition structure
    //     // =====================
    //     Vector<String> fields = new Vector<>();
    //     fields.add("id");
    //     fields.add("name");

    //     Vector<Domain> domains = new Vector<>();
    //     domains.add(new VARCHAR(1).intoDomain()); // id VARCHAR(1)
    //     domains.add(new VARCHAR(1).intoDomain()); // name VARCHAR(1)

    //     // Relation vide
    //     Relation rel = new Relation("relInsert", fields, domains, new Vector<>());

    //     System.out.println("=== Test insertion d'individus ===");

    //     // =====================
    //     // 2. Individus de test
    //     // =====================
    //     Vector<Object> indOK = new Vector<>(Arrays.asList("1", "a"));
    //     Vector<Object> indBadSize = new Vector<>(Arrays.asList("2")); // une seule colonne → erreur
    //     Vector<Object> indBadValue = new Vector<>(Arrays.asList("3", "abc")); // "abc" dépasse VARCHAR(1)

    //     // =====================
    //     // 3. TEST 1 : insertion valide
    //     // =====================
    //     try {
    //         System.out.println("Insertion valide...");
    //         rel.insertNewInd(indOK);
    //         System.out.println("OK : " + rel + "\n");
    //     } catch (Exception e) {
    //         System.out.println("ÉCHEC (devait réussir) : " + e.getMessage());
    //     }

    //     // =====================
    //     // 4. TEST 2 : taille incorrecte
    //     // =====================
    //     try {
    //         System.out.println("Insertion avec taille incorrecte...");
    //         rel.insertNewInd(indBadSize);
    //         System.out.println("ERREUR → ne devait PAS réussir !");
    //     } catch (DomainSupportErr e) {
    //         System.out.println("OK (DomainSupportErr attendu) : " + e.getMessage() + "\n");
    //     } catch (Exception e) {
    //         System.out.println("Autre erreur : " + e.getMessage());
    //     }

    //     // =====================
    //     // 5. TEST 3 : valeur hors domaine
    //     // =====================
    //     try {
    //         System.out.println("Insertion avec valeur hors domaine...");
    //         rel.insertNewInd(indBadValue);
    //         System.out.println("ERREUR → ne devait PAS réussir !");
    //     } catch (DomainOutOfBonds e) {
    //         System.out.println("OK (DomainOutOfBonds attendu) : " + e.getMessage() + "\n");
    //     } catch (Exception e) {
    //         System.out.println("Autre erreur : " + e.getMessage());
    //     }

    //     System.out.println("=== FIN TEST ===");
    // }

    // public static void main(String[] args) throws DomainOutOfBonds {
    //     makeGeneralTest();
    //     makeTestCharVsVarChar();
    // }
}
