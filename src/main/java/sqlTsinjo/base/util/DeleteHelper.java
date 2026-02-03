package sqlTsinjo.base.util;

import java.util.LinkedHashMap;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class DeleteHelper {
    
    private final Relation relation;
    
    public DeleteHelper(Relation relation) {
        this.relation = relation;
    }
    
    public void delete(Expression condition) throws ParseNomException, EvalErr {
        if (condition == null) {
            // Si condition est null, supprimer TOUTES les lignes
            deleteAllRows();
        } else {
            // Sinon, supprimer seulement les lignes qui satisfont la condition
            deleteWithCondition(condition);
        }
    }
    
    private void deleteAllRows() {
        // Vider complètement la relation
        relation.getIndividus().clear();
    }
    
    private void deleteWithCondition(Expression condition) throws ParseNomException, EvalErr {
        SelectCtx ctx = createEvaluationContext();
        Vector<Vector<Object>> remainingRows = new Vector<>();
        
        for (Vector<Object> row : relation.getIndividus()) {
            // Évaluer la condition pour cette ligne
            Object conditionResult = condition.eval(relation, row, ctx);
            boolean shouldDelete = Expression.ObjectIntoBoolean(conditionResult);
            
            if (!shouldDelete) {
                // Garder la ligne si elle ne satisfait PAS la condition
                remainingRows.add(row);
            }
            // Sinon, la ligne est supprimée (non ajoutée à remainingRows)
        }
        
        // Remplacer les données par les lignes restantes
        relation.getIndividus().clear();
        relation.getIndividus().addAll(remainingRows);
    }
    
    private SelectCtx createEvaluationContext() {
        // Créer un contexte d'évaluation minimal
        return new SelectCtx(new LinkedHashMap<>(),null);
    }
}