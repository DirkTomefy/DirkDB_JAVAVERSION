package RDP.err.eval;

import base.err.EvalErr;

public class AmbigousNameErr extends EvalErr  {

    public AmbigousNameErr(String fieldName) {
        super("Can not use this field name/ambigous Name : "+fieldName);
    }

    
}
