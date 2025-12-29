package sqlTsinjo.query.main.delete.token;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.token.Token;

public class DeleteRqstTokenizer {
    public static ParseSuccess<Token> scanDeleteToken(String input) throws ParseNomException {
        ParseSuccess<String> useSign = ParserNomUtil.tagNoCase("fafao").apply(input.trim());
        ParseSuccess<Token> sourceIndicator = SelectTokenizer.scanFromToken(useSign.remaining().trim());
        ParseSuccess<String> databaseName = ParserNomUtil.tagName(sourceIndicator.remaining().trim());
        return new ParseSuccess<>(databaseName.remaining(), Token.deleteSign(databaseName.matched()));
    }
}
