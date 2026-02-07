package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class NullValueErr extends EvalErr {
    public NullValueErr(String context) {
        super("Null value not allowed for: " + context);
    }
}

