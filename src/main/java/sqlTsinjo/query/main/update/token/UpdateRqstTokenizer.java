package sqlTsinjo.query.main.update.token;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.token.Token;

public class UpdateRqstTokenizer {
     public static ParseSuccess<Token> scanUpdateToken(String input) throws ParseNomException {
        ParseSuccess<String> useSign = ParserNomUtil.tagNoCase("HAVAOZY").apply(input.trim());
        ParseSuccess<String> databaseName = ParserNomUtil.tagName(useSign.remaining().trim());
        return new ParseSuccess<>(databaseName.remaining(), Token.updateSign(databaseName.matched()));
    }
    public static ParseSuccess<Token> scanSetToken(String input) throws ParseNomException {
        ParseSuccess<String> sign =ParserNomUtil.tagNoCase("ova").apply(input.trim());
        return new ParseSuccess<>(sign.remaining(),Token.updateSign(null));
    }
   
}
