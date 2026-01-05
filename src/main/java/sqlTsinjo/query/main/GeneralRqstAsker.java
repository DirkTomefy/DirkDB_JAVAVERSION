package sqlTsinjo.query.main;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.DataBaseNotFound;
import sqlTsinjo.query.err.eval.DatabaseNotExistErr;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.err.eval.TableNotFound;
import sqlTsinjo.query.err.parsing.CommandAvailableNotFound;
import sqlTsinjo.query.main.delete.DeleteRqst;
import sqlTsinjo.query.main.delete.token.DeleteRqstTokenizer;
import sqlTsinjo.query.main.insert.InsertRqst;
import sqlTsinjo.query.main.insert.token.InsertRqstTokenizer;
import sqlTsinjo.query.main.select.SelectExpr;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;
import sqlTsinjo.query.main.sqlobject.create.CreateDataBaseRqst;
import sqlTsinjo.query.main.sqlobject.create.CreateObjectRqst;
import sqlTsinjo.query.main.sqlobject.create.token.CreateObjectTokenizer;
import sqlTsinjo.query.main.sqlobject.drop.DropRequest;
import sqlTsinjo.query.main.sqlobject.drop.token.DropRequestTokenizer;
import sqlTsinjo.query.main.sqlobject.show.token.ShowRequestTokenizer;
import sqlTsinjo.query.main.update.UpdateRqst;
import sqlTsinjo.query.main.update.token.UpdateRqstTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.TokenKind;

public class GeneralRqstAsker {

    // Interface pour les handlers de requêtes
    @FunctionalInterface
    private interface RequestHandler {
        void handle(String input, AppContext ctx, Token token)
                throws ParseNomException, EvalErr, IOException;
    }

    // Registre des handlers
    private static final Map<TokenKind, RequestHandler> HANDLERS = new HashMap<>();

    static {
        // Initialisation des handlers
        HANDLERS.put(TokenKind.CREATEDATABASE, GeneralRqstAsker::handleCreateDatabase);
        HANDLERS.put(TokenKind.CREATETABLE, GeneralRqstAsker::handleCreateTable);
        HANDLERS.put(TokenKind.SELECT, GeneralRqstAsker::handleSelect);
        HANDLERS.put(TokenKind.USEDATABASE, GeneralRqstAsker::handleUseDatabase);
        HANDLERS.put(TokenKind.INSERTINTO, GeneralRqstAsker::handleInsert);
        HANDLERS.put(TokenKind.UPDATE, GeneralRqstAsker::handleUpdate);
        HANDLERS.put(TokenKind.DELETE, GeneralRqstAsker::handleDelete);
        HANDLERS.put(TokenKind.DROPOBJECTSQL, GeneralRqstAsker::handleDrop );
        HANDLERS.put(TokenKind.SHOW,GeneralRqstAsker::handleShow);
    }

    public static void askRequest(String input, AppContext ctx)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<Token> token = scanTokenForRequest(input);
        TokenKind status = token.matched().status;

        RequestHandler handler = HANDLERS.get(status);
        if (handler != null) {
            handler.handle(input, ctx, token.matched());
        } else {
            handleUnknownCommand(status);
        }
    }

    public static void handleShow(String input, AppContext ctx, Token token) throws ParseNomException, NoDatabaseSelect, IOException{
        ParseSuccess<Token> asehoy=ShowRequestTokenizer.scanShowObjectSql(input);
        validateNoRemaining(asehoy);
        ObjectSQLEnum toShow = (ObjectSQLEnum) asehoy.matched().value;
        displayRelation(toShow.show(ctx), ctx);
    }

    // ========== IMPLÉMENTATIONS DES HANDLERS ==========

    private static void handleCreateDatabase(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<CreateObjectRqst> result = CreateObjectRqst.parseCreate(input);
        validateNoRemaining(result);

        result.matched().eval(ctx);

        String databaseName = extractDatabaseName(result.matched());
        printSuccess("Ny tahiry : " + databaseName + " dia voaforona soamantsara");
    }

    private static void handleCreateTable(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<CreateObjectRqst> result = CreateObjectRqst.parseCreate(input);
        validateNoRemaining(result);

        result.matched().eval(ctx);
        printSuccess("Ny tabilao dia voaforona soamantsara");
    }

    private static void handleSelect(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<SelectExpr> result = SelectExpr.parseExpr(input);
        validateNoRemaining(result);

        Relation relation = result.matched().eval(ctx);
        displayRelation(relation, ctx);
    }

    private static void handleUseDatabase(String input, AppContext ctx, Token token)
            throws ParseNomException, DatabaseNotExistErr {

        String databaseName = (String) token.value;
        validateNoRemaining(scanUseDatabaseToken(input).remaining());

        ctx.setDatabaseName(databaseName);
        printSuccess("Ny tahiry : " + databaseName + " dia miasa ankehitriny");
    }

    private static void handleInsert(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<InsertRqst> result = InsertRqst.parseInsert(input);
        validateNoRemaining(result);

        result.matched().eval(ctx);
        printSuccess("Tafinditra ny fiovana ny tabilao");
    }

    private static void handleUpdate(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<UpdateRqst> result = UpdateRqst.parseUpdate(input);
        validateNoRemaining(result);

        result.matched().eval(ctx);
        printSuccess("Tafinditra ny fiovana ny tabilao");
    }

    private static void handleDelete(String input, AppContext ctx, Token token)
            throws ParseNomException, EvalErr, IOException {

        ParseSuccess<DeleteRqst> result = DeleteRqst.parseDelete(input);
        validateNoRemaining(result);

        result.matched().eval(ctx);
        printSuccess("Tafinditra ny fiovana ny tabilao");
    }

    public static void handleDrop(String input, AppContext ctx, Token token) throws ParseNomException, DataBaseNotFound,
            DatabaseNotExistErr, TableNotFound, NoDatabaseSelect, IOException {
        ParseSuccess<DropRequest> result = DropRequest.parseDropRequest(input);
        validateNoRemaining(result);
        result.matched().eval(ctx);
    }

    private static void handleUnknownCommand(TokenKind status) {
        System.err.println("Commande non gérée: " + status);
        // Ou lancer une exception selon vos besoins
        throw new UnsupportedOperationException("Commande non supportée: " + status);
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private static void validateNoRemaining(ParseSuccess<?> result) throws ParseNomException {
        if (!result.remaining().trim().isEmpty()) {
            throw ParseNomException.buildRemainingException(result.remaining());
        }
    }

    private static void validateNoRemaining(String input) throws ParseNomException {
        if (!input.trim().isEmpty()) {
            throw ParseNomException.buildRemainingException(input);
        }
    }

    private static String extractDatabaseName(CreateObjectRqst request) {
        if (request instanceof CreateDataBaseRqst) {
            return (String) ((CreateDataBaseRqst) request).getDatabaseName();
        }
        return "Inconnu";
    }

    private static void displayRelation(Relation relation, AppContext ctx) {
        String output = ctx.isDebugMode()
                ? relation.toStringDebug()
                : relation.toString();

        System.out.println("\n" + output + "\n");
    }

    private static void printSuccess(String message) {
        System.out.println(message);
    }

    // ========== MÉTHODES DE TOKENISATION ==========

    public static ParseSuccess<Token> scanUseDatabaseToken(String input) throws ParseNomException {
        ParseSuccess<String> useSign = ParserNomUtil.tagNoCase("AMPIASAO").apply(input.trim());
        ParseSuccess<String> databaseName = ParserNomUtil.tagName(useSign.remaining().trim());
        return new ParseSuccess<>(databaseName.remaining(), Token.useDatabase(databaseName.matched()));
    }

    public static ParseSuccess<Token> scanTokenForRequest(String input) throws ParseNomException {
        try {
            return ParserNomUtil.alt(
                    GeneralRqstAsker::scanUseDatabaseToken,
                    SelectTokenizer::scanSelectToken,
                    CreateObjectTokenizer::scanCreateToken,
                    InsertRqstTokenizer::scanRealInsertToken,
                    UpdateRqstTokenizer::scanUpdateToken,
                    DeleteRqstTokenizer::scanDeleteToken,
                    DropRequestTokenizer::scanDropObjectSql,
                    ShowRequestTokenizer::scanShowObjectSql
                ).apply(input);
        } catch (Exception e) {
            throw new CommandAvailableNotFound(input);
        }
    }
}