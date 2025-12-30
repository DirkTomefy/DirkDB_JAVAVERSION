package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DatabaseNotExistErr extends EvalErr {

    public DatabaseNotExistErr(String databaseName) {
        super("Le database "+databaseName +" existe déja");
    }
    
}
