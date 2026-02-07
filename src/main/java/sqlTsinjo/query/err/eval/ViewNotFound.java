package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class ViewNotFound extends EvalErr {

    public ViewNotFound(String database, String viewName) {
        super("Ny jery " + database + "." + viewName + " dia tsy hita");
    }
    
}
