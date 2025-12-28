package sqlTsinjo.debug.display;
// import java.util.*;

// import sqlTsinjo.base.Domain;
 
// import sqlTsinjo.base.Relation;
// import sqlTsinjo.base.domains.DATE;
// import sqlTsinjo.base.domains.NUMBER;
// import sqlTsinjo.base.domains.VARCHAR;

public class TestAffichage {
//     public static void main(String[] args) {

//         // --- 1) Nom de la relation ---
//         String name = "Employes";

//         // --- 2) Champs ---
//         Vector<String> fieldName = new Vector<>();
//         fieldName.add("Nom");
//         fieldName.add("Age");
//         fieldName.add("Salaire");
//         fieldName.add("DateEmbauche");

//         // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
//         Vector<Domain> domaines = new Vector<>();  // ou new Vector<>()
//         domaines.add((new VARCHAR(10)).intoDomain());
//         domaines.add((new VARCHAR(10)).intoDomain());
//         domaines.add((new NUMBER()).intoDomain());
//         domaines.add((new DATE()).intoDomain());

//         // --- 4) Individus ---
//         Vector< Vector<Object>> individus = new Vector<>();

//         // Individu 1
//         individus.add(new  Vector<Object>(new Vector<>(Arrays.asList(
//                 "Jean", 32, 450000.75, new Date()
//         ))));

//         // Individu 2
//         individus.add(new  Vector<Object>(new Vector<>(Arrays.asList(
//                 "Marie", 25, 380000.00, new Date()
//         ))));

//         // Individu 3 avec un NULL
//         individus.add(new  Vector<Object>(new Vector<>(Arrays.asList(
//                 "Alexandre", null, 500000.00, new Date()
//         ))));

//         // --- 5) Création de la relation ---
//         Relation relation = new Relation(name, fieldName, domaines, individus);

//         // --- 6) Test toString() ---
//         System.out.println(relation);

//         // --- 6) Test toStringDebug() ---
//         System.out.println(""+relation.toStringDebug());
//     }

}
