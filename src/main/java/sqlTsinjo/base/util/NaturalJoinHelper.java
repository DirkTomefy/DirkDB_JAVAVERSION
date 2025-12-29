package sqlTsinjo.base.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.query.main.common.QualifiedIdentifier;

public class NaturalJoinHelper {
    
    private final Relation leftRelation;
    private final Relation rightRelation;
    private final Map<Integer, Integer> commonColumnMap;
    
    public NaturalJoinHelper(Relation left, Relation right) {
        this.leftRelation = left;
        this.rightRelation = right;
        this.commonColumnMap = new HashMap<>();
    }
    
    /**
     * Exécute la jointure naturelle entre les deux relations
     */
    public Relation execute() {
        // 1. Identifier les colonnes communes
        findCommonColumns();
        
        // 2. Si aucune colonne commune, retourner le produit cartésien
        if (commonColumnMap.isEmpty()) {
            return createCartesianProduct();
        }
        
        // 3. Construire le résultat de la jointure naturelle
        return buildNaturalJoinResult();
    }
    
    /**
     * Trouve les colonnes communes entre les deux relations
     */
    private void findCommonColumns() {
        for (int i = 0; i < leftRelation.getFieldName().size(); i++) {
            QualifiedIdentifier leftField = leftRelation.getFieldName().get(i);
            
            for (int j = 0; j < rightRelation.getFieldName().size(); j++) {
                QualifiedIdentifier rightField = rightRelation.getFieldName().get(j);
                
                if (areFieldsEqualForNaturalJoin(leftField, rightField)) {
                    commonColumnMap.put(i, j);
                    break; // Une fois trouvé, on passe à la colonne suivante
                }
            }
        }
    }
    
    /**
     * Vérifie si deux champs sont égaux pour une jointure naturelle
     */
    private boolean areFieldsEqualForNaturalJoin(QualifiedIdentifier field1, QualifiedIdentifier field2) {
        // Comparaison par nom uniquement (sans tenir compte du qualifier)
        return field1.equals(field2) || 
               field1.getName().equals(field2.getName());
    }
    
    /**
     * Crée un produit cartésien quand il n'y a pas de colonnes communes
     */
    private Relation createCartesianProduct() {
        return Relation.produitCartesien(leftRelation, rightRelation);
    }
    
    /**
     * Construit le résultat de la jointure naturelle
     */
    private Relation buildNaturalJoinResult() {
        String newName = generateResultName();
        FieldAndDomainInfo fieldDomainInfo = buildFieldAndDomainInfo();
        Vector<Vector<Object>> joinedRows = performJoin();
        
        return new Relation(newName, 
                           fieldDomainInfo.getFieldNames(), 
                           fieldDomainInfo.getDomains(), 
                           joinedRows);
    }
    
    /**
     * Génère le nom de la relation résultante
     */
    private String generateResultName() {
        return leftRelation.getName() + "_natural_join_" + rightRelation.getName();
    }
    
    /**
     * Construit les nouveaux champs et domaines pour le résultat
     */
    private FieldAndDomainInfo buildFieldAndDomainInfo() {
        FieldAndDomainInfo info = new FieldAndDomainInfo();
        
        // Ajouter tous les champs de la relation gauche
        info.getFieldNames().addAll(leftRelation.getFieldName());
        info.getDomains().addAll(leftRelation.getDomaines());
        
        // Ajouter seulement les champs non-communs de la relation droite
        for (int j = 0; j < rightRelation.getFieldName().size(); j++) {
            if (!commonColumnMap.containsValue(j)) {
                info.getFieldNames().add(rightRelation.getFieldName().get(j));
                info.getDomains().add(rightRelation.getDomaines().get(j));
            }
        }
        
        return info;
    }
    
    /**
     * Effectue la jointure des lignes
     */
    private Vector<Vector<Object>> performJoin() {
        Vector<Vector<Object>> result = new Vector<>();
        
        for (Vector<Object> leftRow : leftRelation.getIndividus()) {
            for (Vector<Object> rightRow : rightRelation.getIndividus()) {
                if (rowsMatch(leftRow, rightRow)) {
                    result.add(buildJoinedRow(leftRow, rightRow));
                }
            }
        }
        
        return result;
    }
    
    /**
     * Vérifie si deux lignes correspondent pour la jointure
     */
    private boolean rowsMatch(Vector<Object> leftRow, Vector<Object> rightRow) {
        return commonColumnMap.entrySet().stream()
            .allMatch(entry -> valuesMatch(
                leftRow.get(entry.getKey()),
                rightRow.get(entry.getValue())
            ));
    }
    
    /**
     * Compare deux valeurs pour l'égalité
     */
    private boolean valuesMatch(Object value1, Object value2) {
        return Relation.objectsEqual(value1, value2);
    }
    
    /**
     * Construit une ligne jointe
     */
    private Vector<Object> buildJoinedRow(Vector<Object> leftRow, Vector<Object> rightRow) {
        Vector<Object> newRow = new Vector<>(leftRow);
        
        // Ajouter seulement les valeurs non-communes de la relation droite
        for (int j = 0; j < rightRow.size(); j++) {
            if (!commonColumnMap.containsValue(j)) {
                newRow.add(rightRow.get(j));
            }
        }
        
        return newRow;
    }
    
    /**
     * Classe interne pour transporter les informations de champs et domaines
     */
    private static class FieldAndDomainInfo {
        private final Vector<QualifiedIdentifier> fieldNames;
        private final Vector<Domain> domains;
        
        public FieldAndDomainInfo() {
            this.fieldNames = new Vector<>();
            this.domains = new Vector<>();
        }
        
        public Vector<QualifiedIdentifier> getFieldNames() {
            return fieldNames;
        }
        
        public Vector<Domain> getDomains() {
            return domains;
        }
    }
    
    // ========== MÉTHODES STATIQUES UTILITAIRES ==========
    
    /**
     * Méthode statique pour une utilisation rapide
     */
    public static Relation naturalJoin(Relation left, Relation right) {
        return new NaturalJoinHelper(left, right).execute();
    }
    
    /**
     * Vérifie si deux relations ont des colonnes communes
     */
    public static boolean hasCommonColumns(Relation left, Relation right) {
        NaturalJoinHelper helper = new NaturalJoinHelper(left, right);
        helper.findCommonColumns();
        return !helper.commonColumnMap.isEmpty();
    }
    
    /**
     * Obtient la liste des noms de colonnes communes
     */
    public static Vector<String> getCommonColumnNames(Relation left, Relation right) {
        Vector<String> commonNames = new Vector<>();
        
        for (int i = 0; i < left.getFieldName().size(); i++) {
            QualifiedIdentifier leftField = left.getFieldName().get(i);
            
            for (int j = 0; j < right.getFieldName().size(); j++) {
                QualifiedIdentifier rightField = right.getFieldName().get(j);
                
                if (leftField.getName().equals(rightField.getName())) {
                    commonNames.add(leftField.getName());
                    break;
                }
            }
        }
        
        return commonNames;
    }
}