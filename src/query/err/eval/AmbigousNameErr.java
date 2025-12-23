package query.err.eval;


import base.err.EvalErr;
import query.main.common.QualifiedIdentifier;

public class AmbigousNameErr extends EvalErr  {

    public AmbigousNameErr(QualifiedIdentifier fieldName) {
        super("Can not use this field name/ambigous Name : "+fieldName);
    }

    
}
