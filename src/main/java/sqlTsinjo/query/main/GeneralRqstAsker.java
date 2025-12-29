package sqlTsinjo.query.main;

import java.io.IOException;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.parsing.CommandAvailableNotFound;
import sqlTsinjo.query.main.insert.InsertRqst;
import sqlTsinjo.query.main.insert.token.InsertRqstTokenizer;
import sqlTsinjo.query.main.select.SelectExpr;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.main.sqlobject.create.CreateDataBaseRqst;
import sqlTsinjo.query.main.sqlobject.create.CreateObjectRqst;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.token.Token;

public class GeneralRqstAsker {
    public static String askRequest(String input, AppContext ctx) throws ParseNomException, EvalErr, IOException {
        ParseSuccess<Token> token = scanTokenForRequest(input);
        
        switch (token.matched().status) {
            case CREATEDATABASE:
                ParseSuccess<CreateObjectRqst> createdatabase = CreateObjectRqst.parseCreate(input);
                createdatabase.matched().eval(ctx);
                input=createdatabase.remaining();
                String databaseName = (String)((CreateDataBaseRqst) createdatabase.matched()).getDatabaseName();
                System.out.println("Ny tahiry : "+ databaseName + " dia voaforona soamantsara ");
                break;
            case CREATETABLE:
                ParseSuccess<CreateObjectRqst> createTable = CreateObjectRqst.parseCreate(input);
                createTable.matched().eval(ctx);
                input=createTable.remaining();
                break;
            case SELECT:
                ParseSuccess<SelectExpr> select = SelectExpr.parseExpr(input);
                Relation rel=select.matched().eval(ctx);
                System.out.println("\n"+rel+"\n");
                input=select.remaining();
                break;
            case USEDATABASE:
                ctx.setDatabaseName((String) token.matched().value);
                input=token.remaining();
                System.out.println("Ny tahiry : "+ctx.getDatabaseName()+ " dia miasa ankehitriny");
                break;
            
            case INSERTINTO:
               ParseSuccess<InsertRqst> insert = InsertRqst.parseInsert(input);
               insert.matched().eval(ctx);
               input=insert.remaining();
                break;
            default:
                break;
            
        }
        return input;
    }

    public static ParseSuccess<Token> scanUseDatabaseToken(String input) throws ParseNomException {
        ParseSuccess<String> useSign = ParserNomUtil.tagNoCase("AMPIASAO").apply(input.trim());
        ParseSuccess<String> databaseName = ParserNomUtil.tagName(useSign.remaining().trim());
        return new ParseSuccess<>(databaseName.remaining(), Token.useDatabase(databaseName.matched()));
    }

    public static ParseSuccess<Token> scanTokenForRequest(String input) throws ParseNomException {
        try {
            return ParserNomUtil.alt(GeneralRqstAsker::scanUseDatabaseToken, SelectTokenizer::scanSelectToken,
                    CreateObjectTokenizer::scanCreateToken,InsertRqstTokenizer::scanRealInsertToken).apply(input);
        } catch (Exception e) {
            throw new CommandAvailableNotFound(input);
        }

    }
}
