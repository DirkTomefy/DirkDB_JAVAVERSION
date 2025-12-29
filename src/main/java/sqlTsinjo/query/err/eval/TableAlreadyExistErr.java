package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class TableAlreadyExistErr extends EvalErr{

    public TableAlreadyExistErr(String tableName) {
        super("La table : "+tableName +" existe déja");
    }
    
}
