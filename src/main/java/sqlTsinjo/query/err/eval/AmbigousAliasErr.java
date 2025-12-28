package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class AmbigousAliasErr extends EvalErr {
    public AmbigousAliasErr(String message) {
        super(message);
    }
}
