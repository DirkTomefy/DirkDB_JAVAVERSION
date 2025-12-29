package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class NoDatabaseSelect extends EvalErr{

    public NoDatabaseSelect() {
        super("Aucune database sélécitioné");
    }
    
}
