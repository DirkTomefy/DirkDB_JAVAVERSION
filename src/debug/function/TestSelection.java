package debug.function;

// import java.util.Arrays;
// import java.util.Date;
// import java.util.Vector;

// import base.Domain;
// import base.Relation;
// import base.domains.DATE;
// import base.domains.NUMBER;
// import base.domains.VARCHAR;
// import base.err.EvalErr;
// import base.err.ParseNomException;

public class TestSelection {
        // public static Relation makeRelationOne() {
        //         // --- 1) Nom de la relation ---
        //         String name = "Employes";

        //         // --- 2) Champs ---
        //         Vector<String> fieldName = new Vector<>();
        //         fieldName.add("Nom");
        //         fieldName.add("Age");
        //         fieldName.add("Salaire");
        //         fieldName.add("DateEmbauche");

        //         // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
        //         Vector<Domain> domaines = new Vector<>(); // ou new Vector<>()
        //         domaines.add((new VARCHAR(10)).intoDomain());
        //         domaines.add((new NUMBER(1, 2)).intoDomain());
        //         domaines.add((new NUMBER()).intoDomain());
        //         domaines.add((new DATE()).intoDomain());

        //         // --- 4) Individus ---
        //         Vector<Vector<Object>> individus = new Vector<>();

        //         // Individu 1
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Jean", 32, 450000.75, new Date()))));

        //         // Individu 2
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Marie", 25, 380000.00, new Date()))));

        //         // Individu 3 avec un NULL
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Alexandre", null, 500000.00, new Date()))));

        //         // --- 5) Création de la relation ---
        //         return new Relation(name, fieldName, domaines, individus);
        // }

        // public static Relation makeRelationTwo() {
        //         // --- 1) Nom de la relation ---
        //         String name = "Employes";

        //         // --- 2) Champs ---
        //         Vector<String> fieldName = new Vector<>();
        //         fieldName.add("Nom");
        //         fieldName.add("Age");
        //         fieldName.add("Salaire");
        //         fieldName.add("Ville");

        //         // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
        //         Vector<Domain> domaines = new Vector<>();
        //         domaines.add((new VARCHAR(10)).intoDomain());
        //         domaines.add((new NUMBER(1, 2)).intoDomain());
        //         domaines.add((new NUMBER()).intoDomain());
        //         domaines.add((new VARCHAR(10)).intoDomain());

        //         // --- 4) Individus ---
        //         Vector<Vector<Object>> individus = new Vector<>();

        //         // Individu 1
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Jean", 32, 450000.75, "Paris"))));

        //         // Individu 2
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Paris", 25, 380000.00, "Paris"))));

        //         // Individu 3 avec un NULL
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         "Alexandre", null, 500000.00, "Paris"))));

        //         // --- 5) Création de la relation ---
        //         return new Relation(name, fieldName, domaines, individus);
        // }

        // public static Relation makeRelationThree() {

        //         // --- 1) Nom de la relation ---
        //         String name = "Codes";

        //         // --- 2) Champs ---
        //         Vector<String> fieldName = new Vector<>();
        //         fieldName.add("CodeChar"); // CHAR(1)
        //         fieldName.add("CodeVar"); // VARCHAR(1)
        //         fieldName.add("Nom"); // VARCHAR(10)

        //         // --- 3) Domaines ---
        //         Vector<Domain> domaines = new Vector<>();
        //         domaines.add((new base.domains.CHAR(1)).intoDomain()); // char[]
        //         domaines.add((new VARCHAR(1)).intoDomain()); // String
        //         domaines.add((new VARCHAR(10)).intoDomain()); // String

        //         // --- 4) Individus ---
        //         Vector<Vector<Object>> individus = new Vector<>();

        //         // Individu 1 : char == "A"
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         new char[] { 'A' }, "A", "Alpha"))));

        //         // Individu 2 : char != "B"
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         new char[] { 'B' }, "A", "Beta"))));

        //         // Individu 3 : coincident "C" == "C"
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         new char[] { 'C' }, "C", "Charlie"))));

        //         // Individu 4 : null tests
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         null, "D", "Delta"))));

        //         // Individu 5 : VARCHAR null
        //         individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
        //                         new char[] { 'E' }, null, "Echo"))));

        //         // --- 5) Création de la relation ---
        //         return new Relation(name, fieldName, domaines, individus);
        // }

        // public static void makeTestSelection(int testId, Relation relation, String condition)
        //                 throws ParseNomException, EvalErr {
        //         System.out.println("Test " + testId + " \n");
        //         System.out.println("Avant selection : \n" + relation.toStringDebug()+"\n");

        //         System.out.println("Aprés selection : '"+condition+"' \n" + relation.selection(condition));

        //         System.out.println("------------------------------------------------------------------------\n\n");
        // }

        // public static void main(String[] args) throws ParseNomException, EvalErr {
        //         int index = 1;
        //         makeTestSelection(index++, makeRelationOne(), "Age is not null");
        //         makeTestSelection(index++, makeRelationTwo(), "((Age is not null) AND Nom=Ville ) and -1=--(-1)");
        //         makeTestSelection(index++, makeRelationThree(), "CodeChar = CodeVar");
        //         makeTestSelection(index++, makeRelationThree(), "CodeChar != CodeVar");
        //         makeTestSelection(index++, makeRelationThree(), "CodeChar is not null AND CodeVar is not null");
        //         makeTestSelection(index++, makeRelationThree(), "Nom = 'Alpha'");

        // }
}
