package query.main.select.token;

import java.util.List;

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
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tag("#"), ParserNomUtil.tagNoCase("ao"),
                        ParserNomUtil.optParser(ParserNomUtil.tag("@")))
                .apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }

    public static ParseSuccess<Token> scanWhereToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tag("#"), ParserNomUtil.tagNoCase("rehefa"))
                .apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.selectSign());
    }

    public static ParseSuccess<Token> scanCommaToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tag(",").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.comma());
    }

    public static ParseSuccess<Token> scanAsToken(String input) throws ParseNomException {
        ParseSuccess<String> success = ParserNomUtil.tagNoCase("antso").apply(input.trim());
        return new ParseSuccess<>(success.remaining(), Token.asSign());
    }

    public static ParseSuccess<Token> scanFieldsToken(String input) throws ParseNomException {
        return ParserNomUtil.alt(
                SelectTokenizer::scanAsToken,
                SelectTokenizer::scanCommaToken,
                Tokenizer.tagId()).apply(input);
    }

    public static ParseSuccess<Token> mapJoinToken(String matched, String input) throws ParseNomException {
        if (matched.isEmpty() || matched.equalsIgnoreCase("anatiny")) {
            return new ParseSuccess<Token>(input, Token.innerJoin());
        } else if (matched.equalsIgnoreCase("ankavia") || matched.equalsIgnoreCase("avia")) {
            return new ParseSuccess<Token>(input, Token.leftJoin());
        } else if (matched.equalsIgnoreCase("avanana") || matched.equalsIgnoreCase("ankavanana")) {
            return new ParseSuccess<Token>(input, Token.rightJoin());
        } else if (matched.equalsIgnoreCase("natoraly")) {
            return new ParseSuccess<Token>(input, Token.naturalJoin());
        } else if (matched.equalsIgnoreCase("feno")) {
            return new ParseSuccess<Token>(input, Token.fullJoin());
        } else {
            throw new ParseNomException(input, "jointure non trouvé pour : '" + matched + "'");
        }
    }

    public static ParseSuccess<Token> scanJoinsToken(String input) throws ParseNomException {
        ParseSuccess<List<String>> success = ParserNomUtil
                .tuple(true, ParserNomUtil.tag("#"), ParserNomUtil.tagNoCase("tonona"))
                .apply(input.trim());;
        ParseSuccess<String> joinIndicator = ParserNomUtil.opt(ParserNomUtil.tag(":"), success.remaining().trim());
        if (joinIndicator.matched() != null) {
            ParseSuccess<String> maybeType = ParserNomUtil.opt(inp -> {
                ParseSuccess<String> takeWhile = ParserNomUtil.tagName( inp);
                return new ParseSuccess<>(takeWhile.remaining(), takeWhile.matched().trim());
            }, joinIndicator.remaining());
            if (maybeType.matched() == null || maybeType.matched().isEmpty()) {
                ParseSuccess<String> last_tag = ParserNomUtil.optParser(ParserNomUtil.tag("@")).apply(maybeType.remaining().trim());
                return new ParseSuccess<>(last_tag.remaining(), Token.innerJoin());
            } else {
                ParseSuccess<String> last_tag = ParserNomUtil.optParser(ParserNomUtil.tag("@")).apply(maybeType.remaining().trim());
                return mapJoinToken(maybeType.matched(), last_tag.remaining());
            }
        } else {
            ParseSuccess<String> last_tag = ParserNomUtil.optParser(ParserNomUtil.tag("@")).apply(joinIndicator.remaining().trim());
            return new ParseSuccess<>(last_tag.remaining(), Token.innerJoin());
        }
    }

    public static void main(String[] args) throws ParseNomException {
        System.out.println("" + scanFromToken("#ao  @"));
    }

}
