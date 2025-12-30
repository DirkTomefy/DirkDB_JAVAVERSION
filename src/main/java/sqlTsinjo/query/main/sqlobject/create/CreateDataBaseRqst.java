package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.DatabaseNotExistErr;

public class CreateDataBaseRqst implements CreateObjectRqst{
    String databaseName;

    public CreateDataBaseRqst(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
        File path = new File("databases/"+databaseName+"/tables");
        if(path.exists()){
            throw new DatabaseNotExistErr(databaseName);
        }else{
            path.mkdirs();
        }
        
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
