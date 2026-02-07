package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class TableNotFound extends EvalErr {

    public TableNotFound(String database , String tableName) {
        super("La table "+database+"."+tableName+" n'as pas été trouvé");
    }
    
}
