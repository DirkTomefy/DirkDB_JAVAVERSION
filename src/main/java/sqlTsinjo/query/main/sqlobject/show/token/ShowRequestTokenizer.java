package sqlTsinjo.query.main.sqlobject.show.token;

import java.util.List;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;
import sqlTsinjo.query.token.Token;

public class ShowRequestTokenizer {
    public static ParseSuccess<Token> scanShowTableToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("asehoy"), ParserNomUtil.tagNoCase("ny"),
                        ParserNomUtil.tagNoCase("tabilao"))
                .apply(input);
        return new ParseSuccess<>(success.remaining(), Token.showListObjectSQL(ObjectSQLEnum.TABLE));
    }

    public static ParseSuccess<Token> scanShowDatabaseToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("asehoy"), ParserNomUtil.tagNoCase("ny"),
                        ParserNomUtil.tagNoCase("tahiry"))
                .apply(input);
        return new ParseSuccess<>(success.remaining(), Token.showListObjectSQL(ObjectSQLEnum.DATABASE));
    }

        public static ParseSuccess<Token> scanShowDomainToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("asehoy"), ParserNomUtil.tagNoCase("ny"),
                        ParserNomUtil.tagNoCase("efitra"))
                .apply(input);
        return new ParseSuccess<>(success.remaining(), Token.showListObjectSQL(ObjectSQLEnum.DOMAINS));
    }

    public static ParseSuccess<Token> scanShowObjectSql(String input) throws ParseNomException {
        return ParserNomUtil.alt(ShowRequestTokenizer::scanShowDatabaseToken, ShowRequestTokenizer::scanShowTableToken,ShowRequestTokenizer::scanShowDomainToken)
                .apply(input);
    }
}
