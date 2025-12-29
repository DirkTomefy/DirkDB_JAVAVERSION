package sqlTsinjo.storage;

import java.io.File;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableNotFound;
import sqlTsinjo.query.main.select.SelectExpr;

public class SerdeRelation {
    AppContext appContext;
    String tableName;

    public SerdeRelation(AppContext appContext, String tableName) {
        this.appContext = appContext;
        this.tableName = tableName;
    }

    public File getFile() throws TableNotFound, NoDatabaseSelect {
        if(appContext.getDatabaseName()==null) throw new NoDatabaseSelect();
        String path = "databases/" + appContext.getDatabaseName() + "/tables/" + tableName + ".json";
        File file = new File(path);
        if (!file.exists()) {
            throw new TableNotFound(appContext.getDatabaseName(), tableName);
        } else {
            return file;
        }
    }

    public void serializeRelation(Relation rel) throws IOException, TableNotFound, NoDatabaseSelect {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(getFile(), rel);
    }

    public Relation deserializeRelation() throws  IOException, TableNotFound, NoDatabaseSelect {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getFile(), Relation.class);
    }

    public static void main(String[] args) throws ParseNomException, EvalErr, IOException {
        ParseSuccess<SelectExpr> select = SelectExpr
                .parseExpr("Alaivo * #ao@ code c1 \n #atifitra@ (Alaivo * #ao@ code) ");
        System.out.println("" + select);
        Relation r = select.matched().eval(new AppContext("test", null));
        System.out.println("" + r.toStringDebug());
    }
}
