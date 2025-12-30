package sqlTsinjo.query.main.sqlobject.drop.token;

import java.util.List;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;
import sqlTsinjo.query.token.Token;

public class DropRequestTokenizer {
        public static ParseSuccess<Token> scanDropTableToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("ravao") ,ParserNomUtil.tagNoCase("ny"),  ParserNomUtil.tagNoCase("tabilao")).apply(input);
        return new ParseSuccess<>(success.remaining(),Token.dropObjectSQL(ObjectSQLEnum.TABLE));
    }

    public static ParseSuccess<Token> scanDropDatabaseToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tagNoCase("ravao"), ParserNomUtil.tagNoCase("ny"),ParserNomUtil.tagNoCase("tahiry")).apply(input);
        return new  ParseSuccess<>(success.remaining(),Token.dropObjectSQL(ObjectSQLEnum.DATABASE)) ;
    }
    public static ParseSuccess<Token> scanDropObjectSql(String input) throws ParseNomException{
        return ParserNomUtil.alt(DropRequestTokenizer::scanDropDatabaseToken,DropRequestTokenizer::scanDropTableToken).apply(input);
    }
}
