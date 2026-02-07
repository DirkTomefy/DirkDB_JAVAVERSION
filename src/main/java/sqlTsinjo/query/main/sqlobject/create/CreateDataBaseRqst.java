package sqlTsinjo.query.main.sqlobject.create;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.DatabaseAlreadyExist;
import sqlTsinjo.storage.TombstoneManager;

public class CreateDataBaseRqst extends CreateObjectRqst{

    public CreateDataBaseRqst(String name) {
        this.name = name;
    }

    @Override
    public void eval(AppContext ctx) throws EvalErr, IOException {
        File dbDir = Path.of(ctx.getDataDirectory(), name).toFile();
        File path = Path.of(ctx.getDataDirectory(), name, "tables").toFile();
        File pathDomains = Path.of(ctx.getDataDirectory(), name, "domains").toFile();
        File pathViews = Path.of(ctx.getDataDirectory(), name, "views").toFile();

        if(path.exists()){
            throw new DatabaseAlreadyExist(name);
        }else{
            dbDir.mkdirs();
            TombstoneManager.clearDatabaseDeletedMarker(dbDir, ctx.getTombstoneConfig());
            path.mkdirs();
            pathDomains.mkdirs();
            pathViews.mkdirs();
        }
        
    }

    public String getDatabaseName() {
        return name;
    }
}
