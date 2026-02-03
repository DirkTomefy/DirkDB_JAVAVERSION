package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.DatabaseAlreadyExist;

public class CreateDataBaseRqst extends CreateObjectRqst{

    public CreateDataBaseRqst(String name) {
        this.name = name;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
        File path = new File("databases/"+name+"/tables");
        File pathDomains = new File("databases/"+name+"/domains");

        if(path.exists()){
            throw new DatabaseAlreadyExist(name);
        }else{
            path.mkdirs();
            pathDomains.mkdirs();
        }
        
    }

    public String getDatabaseName() {
        return name;
    }
}
