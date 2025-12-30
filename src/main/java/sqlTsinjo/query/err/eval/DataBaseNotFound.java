package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DataBaseNotFound extends EvalErr {

    public DataBaseNotFound(String databaseName) {
        super("Database non trouvé : "+databaseName);
    }
    
}
