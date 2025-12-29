package sqlTsinjo.query.main.sqlobject.create.token;

import java.util.List;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.token.Token;

public class CreateObjectTokenizer {
    public static ParseSuccess<Token> scanCreateTableToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("Manamboara"), ParserNomUtil.tagNoCase("tabilao")).apply(input);
        return new ParseSuccess<>(success.remaining(), Token.createTable());
    }

    public static ParseSuccess<Token> scanCreateDatabaseToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("Manamboara"), ParserNomUtil.tagNoCase("tahiry")).apply(input);
        return new ParseSuccess<>(success.remaining(), Token.createDatabase());
    }

    public static ParseSuccess<Token> scanCreateToken(String input) throws ParseNomException {
        return ParserNomUtil.alt(CreateObjectTokenizer::scanCreateDatabaseToken, CreateObjectTokenizer::scanCreateTableToken)
                .apply(input);
    }
}
