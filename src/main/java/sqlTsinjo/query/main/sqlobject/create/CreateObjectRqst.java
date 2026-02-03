package sqlTsinjo.query.main.sqlobject.create;

import java.io.IOException;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;

public abstract class CreateObjectRqst {
    protected String name;
    public static ParseSuccess<CreateObjectRqst> parseCreate(String input) throws ParseNomException {
        ParseSuccess<Token> createToken=CreateObjectTokenizer.scanCreateToken(input);
        ObjectSQLEnum t = (ObjectSQLEnum) createToken.matched().value;
        switch (t) {
            case DATABASE:
                ParseSuccess<String> databaseName=ParserNomUtil.tagName(createToken.remaining().trim());
                return new ParseSuccess<>(databaseName.remaining(),new CreateDataBaseRqst(databaseName.matched()));
            case TABLE:
                ParseSuccess<CreateTableRqst> createSuccess=CreateTableRqst.parseCreateTable(input);
                return new ParseSuccess<>(createSuccess.remaining(),createSuccess.matched());
             case DOMAIN:
                ParseSuccess<CreateDomainRqst> createSuccessDomain=CreateDomainRqst.parseCreateDomain(input);
                return new ParseSuccess<>(createSuccessDomain.remaining(),createSuccessDomain.matched());
            default:
                throw new IllegalArgumentException("ERREUR DE GESTION DES TOKEN");
          
        }
    }
    public abstract void eval(AppContext ctx) throws EvalErr, IOException;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
