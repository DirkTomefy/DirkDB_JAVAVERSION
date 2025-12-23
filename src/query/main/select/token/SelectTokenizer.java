package query.main.select.token;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.helper.ParserNomUtil;
import query.token.Token;
import query.token.Tokenizer;

public class SelectTokenizer extends Tokenizer {
    public static ParseSuccess<Token> scanSelectToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.alt(ParserNomUtil.tagNoCase("ALAIVO")).apply(input);
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }

    public static ParseSuccess<Token> scanFromToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tagNoCase("ao@").apply(input);
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }
    public static  ParseSuccess<Token> scanWhereToken(String input) throws ParseNomException {
         ParseSuccess<String> success = ParserNomUtil.tagNoCase("rehefa").apply(input);
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }
    public static  ParseSuccess<Token>  scanFieldsToken(String input) throws ParseNomException {
        // TODO : commas , fieldName , as sign 
        return null;
    }
}
