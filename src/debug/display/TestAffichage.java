package debug.display;
import java.util.*;

import base.Domain;
import base.Individual;
import base.Relation;
import base.domains.VARCHAR;

public class TestAffichage {
    public static void main(String[] args) {

        // --- 1) Nom de la relation ---
        String name = "Employes";

        // --- 2) Champs ---
        Vector<String> fieldName = new Vector<>();
        fieldName.add("Nom");
        fieldName.add("Age");
        fieldName.add("Salaire");
        fieldName.add("DateEmbauche");

        // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
        Vector<Domain> domaines = new Vector<>();  // ou new Vector<>()
        domaines.add((new VARCHAR()).intoDomain());
        domaines.add((new VARCHAR()).intoDomain());


        // --- 4) Individus ---
        Vector<Individual> individus = new Vector<>();

        // Individu 1
        individus.add(new Individual(new Vector<>(Arrays.asList(
                "Jean", 32, 450000.75, new Date()
        ))));

        // Individu 2
        individus.add(new Individual(new Vector<>(Arrays.asList(
                "Marie", 25, 380000.00, new Date()
        ))));

        // Individu 3 avec un NULL
        individus.add(new Individual(new Vector<>(Arrays.asList(
                "Alexandre", null, 500000.00, new Date()
        ))));

        // --- 5) Création de la relation ---
        Relation relation = new Relation(name, fieldName, domaines, individus);

        // --- 6) Test toString() ---
        System.out.println(relation);

        // --- 6) Test toStringDebug() ---
        System.out.println(""+relation.toStringDebug());
    }

}
