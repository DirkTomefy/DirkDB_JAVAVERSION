package sqlTsinjo.base.util;

import java.io.IOException;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.DomainOutOfBonds;
import sqlTsinjo.base.err.DomainSupportErr;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.base.err.RelationalErr;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.insert.element.abstracts.InsertRqstValues;

public class InsertHelper {
    
    private final Relation relation;
    
    public InsertHelper(Relation relation) {
        this.relation = relation;
    }
    
    public void insert(Vector<String> fieldNames, InsertRqstValues values,AppContext context) 
            throws ParseNomException, RelationalErr, IOException {
        
        validateParameters(fieldNames, values);
        Vector<Vector<Object>> allValues = values.getMultiplyValues(context);
        
        if (fieldNames.isEmpty()) {
            insertAllFields(allValues);
        } else {
            insertSpecificFields(fieldNames, allValues);
        }
    }
    
    private void validateParameters(Vector<String> fieldNames, InsertRqstValues values) {
        if (fieldNames == null) {
            throw new IllegalArgumentException("fieldNames ne peut pas être null");
        }
        if (values == null) {
            throw new IllegalArgumentException("values ne peut pas être null");
        }
    }
    
    private void insertAllFields(Vector<Vector<Object>> allValues) 
            throws DomainOutOfBonds, DomainSupportErr {
        
        for (Vector<Object> rowValues : allValues) {
            validateRowSizeForAllFields(rowValues);
            Vector<Object> newInd = createIndividuForAllFields(rowValues);
            relation.insertNewInd(newInd);
        }
    }
    
    private void validateRowSizeForAllFields(Vector<Object> rowValues) throws DomainOutOfBonds {
        if (rowValues.size() != relation.getDomaines().size()) {
            throw new DomainOutOfBonds(rowValues, relation);
        }
    }
    
    private Vector<Object> createIndividuForAllFields(Vector<Object> rowValues) {
        return new Vector<>(rowValues);
    }
    
    private void insertSpecificFields(Vector<String> fieldNames, Vector<Vector<Object>> allValues) 
            throws DomainOutOfBonds, DomainSupportErr, EvalErr {
        
        Vector<Integer> fieldPositions = mapFieldPositions(fieldNames);
        validateFieldValuesCount(fieldNames, allValues);
        
        for (Vector<Object> rowValues : allValues) {
            Vector<Object> newInd = createIndividuWithPartialFields(fieldPositions, rowValues);
            relation.insertNewInd(newInd);
        }
    }
    
    private Vector<Integer> mapFieldPositions(Vector<String> fieldNames) throws EvalErr {
        Vector<Integer> positions = new Vector<>();
        
        for (String fieldName : fieldNames) {
            int position = findFieldPosition(fieldName);
            positions.add(position);
        }
        
        return positions;
    }
    
    private int findFieldPosition(String fieldName) throws EvalErr {
        for (int i = 0; i < relation.getFieldName().size(); i++) {
            QualifiedIdentifier qi = relation.getFieldName().get(i);
            if (fieldNameMatches(qi, fieldName)) {
                return i;
            }
        }
        throw new EvalErr("Champ '" + fieldName + "' non trouvé dans la relation");
    }
    
    private boolean fieldNameMatches(QualifiedIdentifier qi, String fieldName) {
        return qi.getName().equalsIgnoreCase(fieldName) ;
    }
    
    private void validateFieldValuesCount(Vector<String> fieldNames, Vector<Vector<Object>> allValues) 
            throws EvalErr {
        
        if (!allValues.isEmpty() && fieldNames.size() != allValues.get(0).size()) {
            throw new EvalErr(String.format(
                "Le nombre de champs (%d) ne correspond pas au nombre de valeurs (%d)",
                fieldNames.size(), allValues.get(0).size()
            ));
        }
    }
    
    private Vector<Object> createIndividuWithPartialFields(Vector<Integer> fieldPositions, 
                                                            Vector<Object> rowValues) {
        
        Vector<Object> newInd = createNullInitializedIndividu();
        
        for (int i = 0; i < fieldPositions.size(); i++) {
            int pos = fieldPositions.get(i);
            Object value = rowValues.get(i);
            newInd.set(pos, value);
        }
        
        return newInd;
    }
    
    private Vector<Object> createNullInitializedIndividu() {
        Vector<Object> newInd = new Vector<>();
        for (int i = 0; i < relation.getDomaines().size(); i++) {
            newInd.add(null);
        }
        return newInd;
    }
}