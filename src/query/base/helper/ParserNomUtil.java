package query.base.helper;

import java.util.ArrayList;
import java.util.List;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.ParserNom;
import query.main.common.QualifiedIdentifier;

public class ParserNomUtil {

    // === TAG ===
    public static ParserNom<String> tag(String expected) {
        return input -> {
            if (input.startsWith(expected)) {
                return new ParseSuccess<>(input.substring(expected.length()), expected);
            }
            throw ParseNomException.buildTagException(input, expected);
        };
    }

    // === TAGNOCASE ===
    public static ParserNom<String> tagNoCase(String expected) {
        return input -> {
            if (input.length() < expected.length()) {
                throw new ParseNomException(input, "Entrée trop courte : attendu \"" + expected + "\"");
            }
            String prefix = input.substring(0, expected.length());
            if (prefix.equalsIgnoreCase(expected)) {
                String remaining = input.substring(expected.length());
                return new ParseSuccess<>(remaining, prefix);
            } else {
                throw new ParseNomException(input,
                        "Attendu (sans casse) \"" + expected + "\", trouvé \"" + prefix + "\"");
            }
        };
    }

    // === MULTISPACE1 ===
    public static ParserNom<String> multispace1() {
        return input -> {
            int i = 0;
            while (i < input.length() && Character.isWhitespace(input.charAt(i)))
                i++;
            if (i == 0)
                throw new ParseNomException(input, "Expected at least one whitespace");
            return new ParseSuccess<>(input.substring(i), input.substring(0, i));
        };
    }

    // === MULTISPACE0 ===
    public static ParserNom<String> multispace0() {
        return input -> {
            int i = 0;
            while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
                i++;
            }
            String matched = input.substring(0, i);
            String remaining = input.substring(i);
            return new ParseSuccess<>(remaining, matched);
        };
    }

    // === DIGIT1 ===
    public static ParserNom<String> digit1() {
        return input -> {
            int i = 0;
            while (i < input.length() && Character.isDigit(input.charAt(i)))
                i++;
            if (i == 0)
                throw new ParseNomException(input, "Expected at least one digit");
            return new ParseSuccess<>(input.substring(i), input.substring(0, i));
        };
    }

    // === DIGIT0 ===
    public static ParserNom<String> digit0() {
        return input -> {
            int i = 0;
            while (i < input.length() && Character.isDigit(input.charAt(i)))
                i++;
            return new ParseSuccess<>(input.substring(i), input.substring(0, i));
        };
    }

    // === OPT ===
    public static <T> ParseSuccess<T> opt(ParserNom<T> parser, String input) {
        try {
            return parser.apply(input);
        } catch (ParseNomException e) {
            return new ParseSuccess<>(input, null);
        }
    }

    // === DECIMAL1 ===
    public static ParserNom<Double> decimal1() {
        return input -> {
            ParseSuccess<String> intPartRes = ParserNomUtilHelper.parseIntegerPart(input);
            ParseSuccess<String> fracPartRes = ParserNomUtilHelper.parseFractionPart(intPartRes.remaining());
            return ParserNomUtilHelper.combineIntegerAndFraction(intPartRes, fracPartRes);
        };
    }

    // === IDENTIFIER1 ===
    public static ParserNom<QualifiedIdentifier> identifier1(){
        return input->{
             ParseSuccess<String> originPartRes = ParserNomUtilHelper.parseOriginPart(input);
            ParseSuccess<String> simpleNamePartRes = ParserNomUtilHelper.parseSimpleNamePart(originPartRes.remaining());
            return ParserNomUtilHelper.combineOriginAndName(originPartRes, simpleNamePartRes);
        };
    }
    // === TAKEWHILE1 ===
    public static ParseSuccess<String> takeWhile1(java.util.function.Function<Character, Boolean> predicate, String input) throws ParseNomException {
        int i = 0;
        while (i < input.length() && predicate.apply(input.charAt(i)))
            i++;
        if (i == 0)
            throw new ParseNomException(input, "Expected at least one matching character");
        return new ParseSuccess<>(input.substring(i), input.substring(0, i));
    }

    public static ParseSuccess<String> tagName(String input) throws ParseNomException {
        ParseSuccess<String> firstRes = ParserNomUtilHelper.parseFirstChar(input);
        ParseSuccess<String> restRes = ParserNomUtilHelper.parseRestChars(firstRes.remaining());
        return ParserNomUtilHelper.combineFirstAndRestForTag(firstRes, restRes);
    }

    public static ParserNom<String> tagString(char separator) {
        return input -> {
            ParseSuccess<String> openQuote = tag("" + separator).apply(input);
            String remaining = openQuote.remaining();

            ParseSuccess<String> contentRes = takeWhile1(c -> c != separator, remaining);
            remaining = contentRes.remaining();
            String content = contentRes.matched();

            ParseSuccess<String> closeQuote = tag("" + separator).apply(remaining);
            remaining = closeQuote.remaining();

            return new ParseSuccess<>(remaining, content);
        };
    }

    @SafeVarargs
    public static <T> ParserNom<T> alt(ParserNom<T>... parsers) {
        return input -> {
            ParseNomException lastCause = null;
            for (ParserNom<T> parser : parsers) {
                try {
                    return parser.apply(input);
                } catch (ParseNomException e) {
                    lastCause = e;
                }
            }
            throw lastCause != null ? lastCause : new ParseNomException(input, "No parser matched");
        };
    }

    @SafeVarargs
    public static <T> ParserNom<List<T>> tuple(ParserNom<T>... parsers) {
        return input -> {
            List<T> results = new ArrayList<>();
            String rest = input;

            for (ParserNom<T> parser : parsers) {
                ParseSuccess<T> result = parser.apply(rest);
                results.add(result.matched());
                rest = result.remaining();
            }

            return new ParseSuccess<>(rest, results);
        };
    }
}
