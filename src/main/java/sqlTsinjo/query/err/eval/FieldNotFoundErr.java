package sqlTsinjo.query.err.eval;

import java.util.Vector;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.main.common.QualifiedIdentifier;

public class FieldNotFoundErr extends EvalErr {
    public FieldNotFoundErr(QualifiedIdentifier fieldName) {
        super("Field '" + fieldName + "' not found");
    }
    
    public FieldNotFoundErr(QualifiedIdentifier fieldName, Vector<QualifiedIdentifier> availableFields) {
        super("Field '" + fieldName + "' not found. Available fields: " + availableFields);
    }
}