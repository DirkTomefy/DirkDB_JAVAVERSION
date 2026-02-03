package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class TableAlreadyExistErr extends EvalErr{

    public TableAlreadyExistErr(String tableName) {
        super("Ny tabilao : "+tableName +" dia efa misy");
    }
    
}
