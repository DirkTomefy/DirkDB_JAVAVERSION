package base.util;

import java.util.*;

import base.Domain;
 
import base.Relation;
import query.main.common.QualifiedIdentifier;

public class RelationDisplayer {

    private RelationDisplayer() {
    } 

    public static String display(Relation relation) {
        return buildTable(relation, false);
    }

    
    public static String displayDebug(Relation relation) {
        return buildTable(relation, true);
    }

    // =========================================================================
    // CONSTRUCTION DU TABLEAU FORMATÉ
    // =========================================================================
    private static String buildTable(Relation rel, boolean debug) {

        List<QualifiedIdentifier> columns = rel.getFieldName();
        List<Domain> domaines = rel.getDomaines(); 
        List< Vector<Object>> rows = rel.getIndividus();

        StringBuilder sb = new StringBuilder();

        if (columns == null || columns.isEmpty()) {
            return "Relation: " + rel.getName() + "\nAucune colonne définie.\n";
        }

        int colCount = columns.size();
        int[] width = new int[colCount];

        // 1) Largeur max par colonne (noms + domaines + valeurs)
        for (int i = 0; i < colCount; i++) {
            QualifiedIdentifier header = columns.get(i);
            String h1=debug ? header.getName() : header.getOrigin()+header.getName();
            if (debug && domaines != null && i < domaines.size() && domaines.get(i) != null) {
                h1 += " (" + domaines.get(i) + ")";
            }
            width[i] = h1.length();
        }

        if (rows != null) {
            for ( Vector<Object> ind : rows) {
                if (ind == null || ind == null)
                    continue;
                for (int i = 0; i < ind.size(); i++) {
                    String v = formatValue(ind.get(i));
                    if (debug) {
                        String type = (ind.get(i) == null) ? "null"
                                : ind.get(i).getClass().getSimpleName();
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
            QualifiedIdentifier h = columns.get(i);
            String h2=debug ? h.getName() : h.getOrigin()+h.getName();
            if (debug && domaines != null && i < domaines.size() && domaines.get(i) != null) {
                h2+=" (" + domaines.get(i) + ")";
            }
            headerValues.add(h2);
        }
        sb.append(formatRow(headerValues, width)).append("\n");
        sb.append("-".repeat(totalWidth)).append("\n");

        // 4) Lignes des individus
        if (rows != null && !rows.isEmpty()) {
            for ( Vector<Object> ind : rows) {
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

    // ---- version formatRowFrom Vector<Object> adaptée au debug ----
    private static String formatRowFromIndividual( Vector<Object> ind, int[] width, boolean debug) {
        List<Object> vals = ind;
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
    public static String formatObjectIntoDebugVersion(Object a){
        if(a.getClass().equals(String.class)){
            return "\""+a+"\"";
        }else{
            return a.toString();
        }
    }

}
