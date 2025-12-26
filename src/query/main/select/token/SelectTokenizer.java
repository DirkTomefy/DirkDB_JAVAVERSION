package query.main.select.token;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.helper.ParserNomUtil;
import query.token.Token;
import query.token.Tokenizer;

public class SelectTokenizer extends Tokenizer {
    public static ParseSuccess<Token> scanSelectToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.alt(ParserNomUtil.tagNoCase("ALAIVO")).apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }

    public static ParseSuccess<Token> scanFromToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tagNoCase("ao@").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }
    public static  ParseSuccess<Token> scanWhereToken(String input) throws ParseNomException {
         ParseSuccess<String> success = ParserNomUtil.tagNoCase("rehefa").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }

    //scan token for field
    public static ParseSuccess<Token> scanCommaToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tag(",").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.comma());
    }
    public static ParseSuccess<Token> scanAsToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tagNoCase("antso").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.asSign());
    }
    public static  ParseSuccess<Token>  scanFieldsToken(String input) throws ParseNomException {
        return ParserNomUtil.alt(
           SelectTokenizer::scanAsToken,
           SelectTokenizer::scanCommaToken,
           Tokenizer.tagId()
        ).apply(input);
    }
    
}
