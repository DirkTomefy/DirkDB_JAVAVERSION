package sqlTsinjo.query.main.sqlobject.create;

import java.io.IOException;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;

public interface CreateObjectRqst {
    public static ParseSuccess<CreateObjectRqst> parseCreate(String input) throws ParseNomException {
        ParseSuccess<Token> createToken=CreateObjectTokenizer.scanCreateToken(input);
        switch (createToken.matched().status) {
            case CREATEDATABASE:
                ParseSuccess<String> databaseName=ParserNomUtil.tagName(createToken.remaining().trim());
                return new ParseSuccess<>(databaseName.remaining(),new CreateDataBaseRqst(databaseName.matched()));
            case CREATETABLE:
                ParseSuccess<CreateTableRqst> createSuccess=CreateTableRqst.parseCreateTable(input);
                return new ParseSuccess<>(createSuccess.remaining(),createSuccess.matched());
            default:
                throw new IllegalArgumentException("ERREUR DE GESTION DES TOKEN");
          
        }
    }
    public void eval(AppContext ctx) throws EvalErr, IOException;
}
