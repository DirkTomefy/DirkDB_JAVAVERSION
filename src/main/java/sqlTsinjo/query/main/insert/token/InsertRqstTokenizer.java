package sqlTsinjo.query.main.insert.token;

import java.util.List;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.token.Token;

public class InsertRqstTokenizer {
    public static ParseSuccess<Token> scanInsertToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.alt(ParserNomUtil.tagNoCase("MANAMPIA")).apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.insertSign());
    }

    public static ParseSuccess<Token> scanFromToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.optParser(ParserNomUtil.tag("#")), ParserNomUtil.tagNoCase("ao"),
                        ParserNomUtil.optParser(ParserNomUtil.tag("@")))
                .apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.insertSign());
    }

    public static ParseSuccess<Token> scanRealInsertToken(String input) throws ParseNomException {
        var _try = ParserNomUtil.tuple(true, InsertRqstTokenizer::scanInsertToken, InsertRqstTokenizer::scanFromToken)
                .apply(input.trim());
        return new ParseSuccess<Token>(_try.remaining(), Token.insertSign());
    }

    public static ParseSuccess<Token> scanValuesToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.optParser(ParserNomUtil.tag("#")), ParserNomUtil.tagNoCase("sanda"))
                .apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.insertSign());
    }

}
