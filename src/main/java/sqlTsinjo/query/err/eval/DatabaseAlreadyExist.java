package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DatabaseAlreadyExist extends EvalErr{

    public DatabaseAlreadyExist(String databaseName) {
        super("Ny tahiry "+"("+databaseName+")" +" dia efa misy");
    }
    
}
