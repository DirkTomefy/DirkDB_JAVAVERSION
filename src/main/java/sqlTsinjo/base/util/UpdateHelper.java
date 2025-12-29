package sqlTsinjo.base.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.DomainOutOfBonds;
import sqlTsinjo.base.err.DomainSupportErr;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class UpdateHelper {
    
    private final Relation relation;
    
    public UpdateHelper(Relation relation) {
        this.relation = relation;
    }
    
    public void update(HashMap<String, Expression> values, Expression condition) 
            throws ParseNomException, EvalErr, DomainOutOfBonds, DomainSupportErr {
        
        validateParameters(values);
        
        if (condition == null) {
            updateAllRows(values);
        } else {
            updateWithCondition(values, condition);
        }
    }
    
    private void validateParameters(HashMap<String, Expression> values) {
        if (values == null) {
            throw new IllegalArgumentException("values ne peut pas être null");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Aucune valeur à mettre à jour");
        }
    }
    
    private void updateAllRows(HashMap<String, Expression> values) 
            throws EvalErr, DomainOutOfBonds, DomainSupportErr {
        
        Vector<Vector<Object>> updatedRows = new Vector<>();
        
        for (Vector<Object> originalRow : relation.getIndividus()) {
            Vector<Object> updatedRow = calculateUpdatedRow(originalRow, values);
            validateRowDomain(updatedRow);
            updatedRows.add(updatedRow);
        }
        
        // Remplacer toutes les lignes
        relation.getIndividus().clear();
        relation.getIndividus().addAll(updatedRows);
    }
    
    private void updateWithCondition(HashMap<String, Expression> values, Expression condition) 
            throws ParseNomException, EvalErr, DomainOutOfBonds, DomainSupportErr {
        
        SelectCtx ctx = createEvaluationContext();
        Vector<Vector<Object>> updatedIndividus = new Vector<>();
        
        for (Vector<Object> originalRow : relation.getIndividus()) {
            // Évaluer la condition avec la ligne ORIGINALE
            Object conditionResult = condition.eval(relation, originalRow, ctx);
            boolean shouldUpdate = Expression.ObjectIntoBoolean(conditionResult);
            
            if (shouldUpdate) {
                Vector<Object> updatedRow = calculateUpdatedRow(originalRow, values);
                validateRowDomain(updatedRow);
                updatedIndividus.add(updatedRow);
            } else {
                // Garder la ligne originale
                updatedIndividus.add(originalRow);
            }
        }
        
        // Remplacer toutes les lignes
        relation.getIndividus().clear();
        relation.getIndividus().addAll(updatedIndividus);
    }
    
    private Vector<Object> calculateUpdatedRow(Vector<Object> originalRow, 
                                               HashMap<String, Expression> values) 
            throws EvalErr {
        
        SelectCtx ctx = createEvaluationContext();
        Vector<Object> updatedRow = new Vector<>(originalRow);
        
        // Pour chaque champ spécifié dans le HashMap, calculer la nouvelle valeur
        // TOUJOURS en utilisant la ligne ORIGINALE
        for (Map.Entry<String, Expression> entry : values.entrySet()) {
            String fieldName = entry.getKey();
            Expression expr = entry.getValue();
            
            int fieldIndex = findFieldIndex(fieldName);
            if (fieldIndex == -1) {
                throw new EvalErr("Champ '" + fieldName + "' non trouvé dans la relation " + relation.getName());
            }
            
            // Évaluer l'expression avec la ligne ORIGINALE
            Object newValue = expr.eval(relation, originalRow, ctx);
            updatedRow.set(fieldIndex, newValue);
        }
        
        return updatedRow;
    }
    
    private int findFieldIndex(String fieldName) {
        for (int i = 0; i < relation.getFieldName().size(); i++) {
            QualifiedIdentifier qi = relation.getFieldName().get(i);
            if (fieldNameMatches(qi, fieldName)) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean fieldNameMatches(QualifiedIdentifier qi, String fieldName) {
        return qi.getName().equalsIgnoreCase(fieldName) ;
    }
    
    private void validateRowDomain(Vector<Object> row) throws DomainOutOfBonds, DomainSupportErr {
        relation.supportsWithErr(row);
    }
    
    private SelectCtx createEvaluationContext() {
        // Créer un contexte d'évaluation minimal
        // À adapter selon vos besoins
        return new SelectCtx(new LinkedHashMap<>(), null );
    }
}