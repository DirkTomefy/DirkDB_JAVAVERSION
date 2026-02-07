package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class ViewAlreadyExistErr extends EvalErr {

    public ViewAlreadyExistErr(String viewName) {
        super("Ny jery : " + viewName + " dia efa misy");
    }
    
}
