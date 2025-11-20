package RDP.err.eval;

import java.util.Vector;

import RDP.err.EvalErr;

public class FieldNotFoundErr extends EvalErr {
    public FieldNotFoundErr(String fieldName) {
        super("Field '" + fieldName + "' not found");
    }
    
    public FieldNotFoundErr(String fieldName, Vector<String> availableFields) {
        super("Field '" + fieldName + "' not found. Available fields: " + availableFields);
    }
}