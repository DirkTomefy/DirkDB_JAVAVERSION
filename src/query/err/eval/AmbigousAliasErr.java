package query.err.eval;

import base.err.EvalErr;

public class AmbigousAliasErr extends EvalErr {
    public AmbigousAliasErr(String message) {
        super(message);
    }
}
