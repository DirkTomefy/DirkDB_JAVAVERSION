package sqlTsinjo.debug.function;

import java.util.Arrays;
import java.util.Date;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.domains.DATE;
import sqlTsinjo.base.domains.NUMBER;
import sqlTsinjo.base.domains.VARCHAR;
import sqlTsinjo.query.main.common.QualifiedIdentifier;

public class TestSelection {
        public static Relation makeRelationOne() {
                // --- 1) Nom de la relation ---
                String name = "Employes";

                // --- 2) Champs ---
                Vector<QualifiedIdentifier> fieldName = new Vector<>();
                fieldName.add(QualifiedIdentifier.buildFromName("Nom"));
                fieldName.add(QualifiedIdentifier.buildFromName("Age"));
                fieldName.add(QualifiedIdentifier.buildFromName("Salaire"));
                fieldName.add(QualifiedIdentifier.buildFromName("DateEmbauche"));

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
                Vector<QualifiedIdentifier> fieldName = new Vector<>();
                fieldName.add(QualifiedIdentifier.buildFromName("Nom"));
                fieldName.add(QualifiedIdentifier.buildFromName("Age"));
                fieldName.add(QualifiedIdentifier.buildFromName("Salaire"));
                fieldName.add(QualifiedIdentifier.buildFromName("Ville"));

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

        public static Relation makeRelationThree() {

                // --- 1) Nom de la relation ---
                String name = "Codes";

                // --- 2) Champs ---
                Vector<QualifiedIdentifier> fieldName = new Vector<>();
                fieldName.add(QualifiedIdentifier.buildFromName(("CodeChar"))); // CHAR(1)
                fieldName.add(QualifiedIdentifier.buildFromName("CodeVar")); // VARCHAR(1)
                fieldName.add(QualifiedIdentifier.buildFromName("Nom")); // VARCHAR(10)

                // --- 3) Domaines ---
                Vector<Domain> domaines = new Vector<>();
                domaines.add((new sqlTsinjo.base.domains.CHAR(1)).intoDomain()); // char[]
                domaines.add((new VARCHAR(1)).intoDomain()); // String
                domaines.add((new VARCHAR(10)).intoDomain()); // String

                // --- 4) Individus ---
                Vector<Vector<Object>> individus = new Vector<>();

                // Individu 1 : char == "A"
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                new char[] { 'A' }, "A", "Alpha"))));

                // Individu 2 : char != "B"
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                new char[] { 'B' }, "A", "Beta"))));

                // Individu 3 : coincident "C" == "C"
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                new char[] { 'C' }, "C", "Charlie"))));

                // Individu 4 : null tests
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                null, "D", "Delta"))));

                // Individu 5 : VARCHAR null
                individus.add(new Vector<Object>(new Vector<>(Arrays.asList(
                                new char[] { 'E' }, null, "Echo"))));

                // --- 5) Création de la relation ---
                return new Relation(name, fieldName, domaines, individus);
        }
}
