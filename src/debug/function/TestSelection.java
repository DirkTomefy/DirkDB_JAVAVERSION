package debug.function;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import base.Domain;
import base.Relation;
import base.domains.DATE;
import base.domains.NUMBER;
import base.domains.VARCHAR;
import base.err.DomainOutOfBonds;
import base.err.DomainSupportErr;
import base.err.EvalErr;
import base.err.ParseNomException;

public class TestSelection {
        public static Relation makeRelationOne() {
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

        public static Relation makeRelationTwo() {
                // --- 1) Nom de la relation ---
                String name = "Employes";

                // --- 2) Champs ---
                Vector<String> fieldName = new Vector<>();
                fieldName.add("Nom");
                fieldName.add("Age");
                fieldName.add("Salaire");
                fieldName.add("Ville");

                // --- 3) Domaines (ici tu as dit qu’on peut ne pas en mettre) ---
                Vector<Domain> domaines = new Vector<>();
                domaines.add((new VARCHAR(10)).intoDomain());
                domaines.add((new NUMBER(1, 2)).intoDomain());
                domaines.add((new NUMBER()).intoDomain());
                domaines.add((new VARCHAR(10)).intoDomain());

                // --- 4) Individus ---
                Vector<Vector<Object>> individus = new Vector<>();

                // Individu 1
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Jean", 32, 450000.75, "Paris"))));

                // Individu 2
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Paris", 25, 380000.00, "Paris"))));

                // Individu 3 avec un NULL
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                "Alexandre", null, 500000.00, "Paris"))));

                // --- 5) Création de la relation ---
                return new Relation(name, fieldName, domaines, individus);
        }

        public static void makeTestSelection(int testId,Relation relation,String condition) throws ParseNomException, EvalErr{
                System.out.println("Test "+testId+" : \n");
                System.out.println("Avant selection : \n" + relation.toStringDebug());

                System.out.println("Aprés selection : \n" + relation.selection(condition));

                System.out.println("------------------------------------------------------------------------\n\n");
        }


        public static void main(String[] args) throws ParseNomException, EvalErr, DomainOutOfBonds, DomainSupportErr {
                int index=1;
                makeTestSelection(index++, makeRelationOne(), "Age is not null");
                makeTestSelection(index++, makeRelationTwo(), "((Age is not null) AND Nom=Ville ) and -1=--(-1)");

        }
}
