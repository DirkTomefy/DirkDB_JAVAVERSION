package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DatabaseAlreadyExistErr extends EvalErr {

    public DatabaseAlreadyExistErr(String databaseName) {
        super("Le database "+databaseName +" existe déja");
    }
    
}
