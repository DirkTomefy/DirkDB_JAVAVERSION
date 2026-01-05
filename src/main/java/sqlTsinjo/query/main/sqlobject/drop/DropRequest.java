package sqlTsinjo.query.main.sqlobject.drop;
import java.io.IOException;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.DatabaseNotExistErr;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableNotFound;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;
import sqlTsinjo.query.main.sqlobject.drop.token.DropRequestTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeRelation;

public class DropRequest {
    String name;
    ObjectSQLEnum objectType;
    public DropRequest(String name, ObjectSQLEnum objectType) {
        this.name = name;
        this.objectType = objectType;
    }
    public static ParseSuccess<DropRequest> parseDropRequest(String input) throws ParseNomException{
        ParseSuccess<Token> t=DropRequestTokenizer.scanDropObjectSql(input);
        ParseSuccess<String> name=ParserNomUtil.tagName(t.remaining());
        ObjectSQLEnum sqlObject = (ObjectSQLEnum) t.matched().value;
        return new ParseSuccess<>(name.remaining(), new DropRequest(name.matched(),sqlObject));
    }
    public  void eval(AppContext ctx) throws DatabaseNotExistErr, IOException, TableNotFound, NoDatabaseSelect{
        SerdeRelation serde =new SerdeRelation(ctx, null );
        switch (objectType) {
            case DATABASE:
                serde.dropDatabase(name);
                break;
            case TABLE:
                serde.setTableName(name);
                serde.dropTable();
                break;
            default:
                break;
        }
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ObjectSQLEnum getObjectType() {
        return objectType;
    }
    public void setObjectType(ObjectSQLEnum objectType) {
        this.objectType = objectType;
    }
}
