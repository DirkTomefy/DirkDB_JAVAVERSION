package query.err.eval;

import java.util.Vector;

import base.err.EvalErr;
import query.main.common.QualifiedIdentifier;

public class FieldNotFoundErr extends EvalErr {
    public FieldNotFoundErr(QualifiedIdentifier fieldName) {
        super("Field '" + fieldName + "' not found");
    }
    
    public FieldNotFoundErr(QualifiedIdentifier fieldName, Vector<QualifiedIdentifier> availableFields) {
        super("Field '" + fieldName + "' not found. Available fields: " + availableFields);
    }
}