package debug.function;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import RDP.err.eval.FieldNotFoundErr;
import RDP.err.eval.FieldToProjectEmpty;
import base.Domain;
import base.Relation;
import base.domains.DATE;
import base.domains.NUMBER;
import base.domains.VARCHAR;

public class TestProjection {
    public static Relation makeRelationTest(){
        // --- 1) Nom de la relation ---
                String name = "Employes";

                // --- 2) Champs ---
                Vector<String> fieldName = new Vector<>();
                fieldName.add("Nom");
                fieldName.add("Age");
                fieldName.add("Salaire");
                fieldName.add("DateEmbauche");

                // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
                Vector<Domain> domaines = new Vector<>(); // ou new Vector<>()
                domaines.add((new VARCHAR(10)).intoDomain());
                domaines.add((new NUMBER(1, 2)).intoDomain());
                domaines.add((new NUMBER()).intoDomain());
                domaines.add((new DATE()).intoDomain());

                // --- 4) Individus ---
                Vector<Vector<Object>> individus = new Vector<>();

                // Individu 1
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Jean", 32, 450000.75, new Date()))));

                // Individu 2
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Marie", 25, 380000.00, new Date()))));

                // Individu 3 avec un NULL
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Alexandre", null, 500000.00, new Date()))));

                // --- 5) Création de la relation ---
                return new Relation(name, fieldName, domaines, individus);
    }
    public static void main(String[] args) throws FieldNotFoundErr, FieldToProjectEmpty {
        Relation rel=makeRelationTest();
        System.out.println("Nom et age : \n "+rel.projection(new String[]{"Nom","Age"})+"\n");

        System.out.println("Nom: \n "+rel.projection(new String[]{"Nom"})+"\n");
    }
}
