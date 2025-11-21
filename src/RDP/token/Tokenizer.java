package RDP.token;

import java.util.List;
import RDP.base.ParseSuccess;
import RDP.base.ParserNom;
import RDP.base.function.operand.PrefixedOp;
import RDP.base.function.operand.other.ArithmeticOp;
import RDP.base.function.operand.other.CompareOp;
import RDP.base.function.operand.other.LogicalOp;
import RDP.base.helper.ParserNomUtil;
import RDP.err.ParseNomException;
import RDP.err.parsing.token.TokenNotFound;

public class Tokenizer {
    public static final String[] privatizedToken = new String[] { "null" };

    public static boolean isPrivatized(String t){
        for (String string : privatizedToken) {  
            if(string.equalsIgnoreCase(t)){
                return true;
            } 
        }
        return false;
    }

    public static ParseSuccess<Token> mapToBinOpToken(ParseSuccess<String> success, String oldInput)
            throws ParseNomException {
        ArithmeticOp op = switch (success.matched()) {
            case "+" -> ArithmeticOp.ADD;
            case "-" -> ArithmeticOp.MIN;
            case "*" -> ArithmeticOp.MUL;
            case "/" -> ArithmeticOp.DIV;
            default -> throw new ParseNomException(oldInput, "Unknown operator: " + success.matched());
        };
        return new ParseSuccess<>(success.remaining(), Token.binop(op));
    }

    public static ParseSuccess<Token> mapToCompareOpToken(ParseSuccess<String> success, String oldInput)
            throws ParseNomException {
        CompareOp op = switch (success.matched()) {
            case "=" -> CompareOp.Eq;
            case "!=" -> CompareOp.Neq;
            case ">=" -> CompareOp.Gte;
            case ">" -> CompareOp.Gt;
            case "<=" -> CompareOp.Lte;
            case "<" -> CompareOp.Lt;
            default -> {
                if ("is".equalsIgnoreCase(success.matched()))
                    yield CompareOp.Is;
                else
                    throw new ParseNomException(oldInput,
                            "Unknown operator: " + success.matched());
            }
        };
        return new ParseSuccess<>(success.remaining(), Token.binop(op));
    }

    public static ParseSuccess<Token> mapToPrefixedOpToken(ParseSuccess<String> success, String oldInput)
            throws ParseNomException {
        PrefixedOp op = switch (success.matched()) {
            case "-" -> PrefixedOp.NEG;
            case "!" -> PrefixedOp.NOT;
            default -> throw new ParseNomException(oldInput,
                    "Unknown operator: " + success.matched());
        };
        return new ParseSuccess<>(success.remaining(), Token.prefixedop(op));
    }

    public static ParseSuccess<Token> mapToLogicalOpToken(ParseSuccess<String> success) throws ParseNomException {
        LogicalOp op = switch (success.matched().toLowerCase()) {
            case "and" -> LogicalOp.AND;
            case "or" -> LogicalOp.OR;
            default -> throw new ParseNomException(success.remaining(), "Unknown operator: " + success.matched());
        };
        return new ParseSuccess<>(success.remaining(), Token.binop(op));
    }

    // === Parsers ===
    public static ParserNom<Token> tagArithmOp() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.alt(
                    ParserNomUtil.tag("+"),
                    ParserNomUtil.tag("-"),
                    ParserNomUtil.tag("*"),
                    ParserNomUtil.tag("/")).apply(input);
            return mapToBinOpToken(success, input);
        };
    }

    public static ParserNom<Token> tagCompareOp() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.alt(
                    ParserNomUtil.tag("<="),
                    ParserNomUtil.tag(">="),
                    ParserNomUtil.tag("!="),
                    ParserNomUtil.tag("<"),
                    ParserNomUtil.tag(">"),
                    ParserNomUtil.tag("="),
                    ParserNomUtil.tagNoCase("is")).apply(input);
            return mapToCompareOpToken(success, input);
        };
    }

    public static ParserNom<Token> tagLogicalOp() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.alt(
                    ParserNomUtil.tagNoCase("and"),
                    ParserNomUtil.tagNoCase("or")).apply(input);
            return mapToLogicalOpToken(success);
        };
    }

    public static ParserNom<Token> tagPrefixedOp() {
        return input -> {
            ParseSuccess<String> s = ParserNomUtil.alt(
                    ParserNomUtil.tag("!"),
                    ParserNomUtil.tag("-")).apply(input);

            return mapToPrefixedOpToken(s, input);
        };
    }

    public static ParserNom<Token> tagIsNot() {
        return input -> {
            ParseSuccess<List<String>> successList = ParserNomUtil.tuple(
                    ParserNomUtil.tagNoCase("is"),
                    ParserNomUtil.multispace1(),
                    ParserNomUtil::tagName).apply(input);

            String combined = String.join("", successList.matched()).replace(" ", "");

            return switch (combined.toLowerCase()) {
                case "isnot" -> new ParseSuccess<>(successList.remaining(), Token.binop(CompareOp.IsNot));
                default -> throw new ParseNomException(input, "Keyword expected");
            };
        };
    }

    // === tagString ===
    public static ParserNom<Token> tagString() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.alt(
                    ParserNomUtil.tagString('"'),
                    ParserNomUtil.tagString('\'')).apply(input);

            return new ParseSuccess<>(success.remaining(), new Token(TokenKind.STRING, success.matched()));
        };
    }

    // === tagId ===
    public static ParserNom<Token> tagId() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.tagName(input);
            if(isPrivatized(success.matched())) throw new ParseNomException(input, "Can not use this '"+success.matched()+"' as an ID because it's a privatized token");
            return new ParseSuccess<>(success.remaining(), Token.id(success.matched()));
        };
    }

    // === tagNumber ===
    public static ParserNom<Token> tagNumber() {
        return input -> {
            ParseSuccess<Double> success = ParserNomUtil.decimal1().apply(input);
            return new ParseSuccess<>(success.remaining(), Token.number(success.matched()));
        };
    }

    // === tagNullValue ===
    public static ParserNom<Token> tagNullValue() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.tagNoCase("null").apply(input);
            return new ParseSuccess<>(success.remaining(), Token.nullvalue());
        };
    }

    public static ParserNom<Token> tagParensToken() {
        return input -> {
            ParseSuccess<String> success = ParserNomUtil.alt(
                    ParserNomUtil.tag("("),
                    ParserNomUtil.tag(")")).apply(input);
            return new ParseSuccess<>(success.remaining(), Token.other(success.matched()));
        };
    }

    public static ParseSuccess<Token> scanFactorToken(String input) throws TokenNotFound {
        input = input.trim();

        try {
            ParseSuccess<Token> t = ParserNomUtil.alt(
                    tagString(),
                    tagId(),
                    tagNumber(),
                    tagNullValue(),
                    tagParensToken(),
                    tagPrefixedOp()).apply(input);
            return t;
        } catch (ParseNomException e) {
            throw new TokenNotFound(input);
        }
    }

    public static ParseSuccess<Token> scanBinopToken(String input) throws TokenNotFound {
        input=input.trim();
        try {
            ParseSuccess<Token> t = ParserNomUtil.alt(
                    tagIsNot(),
                    tagLogicalOp(),
                    tagCompareOp(),
                    tagArithmOp()).apply(input);
            return t;
        } catch (ParseNomException e) {
            throw new TokenNotFound(input);
        }
    }

    public static boolean codonStop(String input) {
        return input.trim().startsWith(")") || input.trim().isEmpty();
    }
}
