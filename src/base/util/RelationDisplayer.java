package base.util;

import java.util.*;

import base.Domain;
import base.Individual;
import base.Relation;

public class RelationDisplayer {

    private RelationDisplayer() {
    } 

    // Affichage normal (sans domaines)
    public static String display(Relation relation) {
        return buildTable(relation, false);
    }

    // Affichage debug (types, infos)
    public static String displayDebug(Relation relation) {
        return buildTable(relation, true);
    }

    // =========================================================================
    // CONSTRUCTION DU TABLEAU FORMATÉ
    // =========================================================================
    private static String buildTable(Relation rel, boolean debug) {

        List<String> columns = rel.getFieldName();
        List<Domain> domaines = rel.getDomaines(); // domaine par colonne
        List<Individual> rows = rel.getIndividus();

        StringBuilder sb = new StringBuilder();

        if (columns == null || columns.isEmpty()) {
            return "Relation: " + rel.getName() + "\nAucune colonne définie.\n";
        }

        int colCount = columns.size();
        int[] width = new int[colCount];

        // 1) Largeur max par colonne (noms + domaines + valeurs)
        for (int i = 0; i < colCount; i++) {
            String header = columns.get(i);
            if (debug && domaines != null && i < domaines.size() && domaines.get(i) != null) {
                header += " (" + domaines.get(i) + ")";
            }
            width[i] = header.length();
        }

        if (rows != null) {
            for (Individual ind : rows) {
                if (ind == null || ind.getValues() == null)
                    continue;
                for (int i = 0; i < ind.getValues().size(); i++) {
                    String v = formatValue(ind.getValues().get(i));
                    if (debug) {
                        String type = (ind.getValues().get(i) == null) ? "null"
                                : ind.getValues().get(i).getClass().getSimpleName();
                        v += " (" + type + ")";
                    }
                    width[i] = Math.max(width[i], v.length());
                }
            }
        }

        int totalWidth = 2 + Arrays.stream(width).sum() + (3 * colCount);

        // 2) En-tête
        sb.append("Relation: ").append(rel.getName()).append("\n");
        sb.append("=".repeat(totalWidth)).append("\n");

        // 3) Ligne en-tête
        List<String> headerValues = new ArrayList<>();
        for (int i = 0; i < colCount; i++) {
            String h = columns.get(i);
            if (debug && domaines != null && i < domaines.size() && domaines.get(i) != null) {
                h += " (" + domaines.get(i) + ")";
            }
            headerValues.add(h);
        }
        sb.append(formatRow(headerValues, width)).append("\n");
        sb.append("-".repeat(totalWidth)).append("\n");

        // 4) Lignes des individus
        if (rows != null && !rows.isEmpty()) {
            for (Individual ind : rows) {
                sb.append(formatRowFromIndividual(ind, width, debug)).append("\n");
            }
        } else {
            sb.append(center("Aucune donnée", totalWidth)).append("\n");
        }

        // 5) Ligne finale + résumé
        sb.append("-".repeat(totalWidth)).append("\n");
        sb.append("Total: ").append(rows != null ? rows.size() : 0).append(" individu(s)");

        return sb.toString();
    }

    // ---- version formatRowFromIndividual adaptée au debug ----
    private static String formatRowFromIndividual(Individual ind, int[] width, boolean debug) {
        List<Object> vals = ind.getValues();
        StringBuilder sb = new StringBuilder("| ");

        for (int i = 0; i < width.length; i++) {
            Object value = (vals != null && i < vals.size()) ? vals.get(i) : null;
            String v = formatValue(value);

            if (debug) {
                String type = (value == null) ? "null" : value.getClass().getSimpleName();
                v += " (" + type + ")";
            }

            sb.append(String.format("%-" + width[i] + "s", v)).append(" | ");
        }
        return sb.toString();
    }

     private static String center(String text, int totalWidth) {
        int padding = (totalWidth - text.length()) / 2;
        return " ".repeat(Math.max(0, padding)) + text;
    }

    private static String formatValue(Object v) {
        if (v == null) return "NULL";
        if (v instanceof String) return "\"" + v + "\"";
        if (v instanceof char[] c) return new String(c);
        return v.toString();
    }

    private static String formatRow(List<String> values, int[] width) {
        StringBuilder sb = new StringBuilder("| ");
        for (int i = 0; i < width.length; i++) {
            String v = (i < values.size() && values.get(i) != null) ? values.get(i) : "";
            sb.append(String.format("%-" + width[i] + "s", v)).append(" | ");
        }
        return sb.toString();
    }

}
