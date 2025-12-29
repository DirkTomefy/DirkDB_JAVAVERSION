package sqlTsinjo.query.base.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.ParserNom;
import sqlTsinjo.query.main.common.QualifiedIdentifier;

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
    public static ParseSuccess<String> multispace1(String input) throws ParseNomException {
        int i = 0;
        while (i < input.length() && Character.isWhitespace(input.charAt(i)))
            i++;
        if (i == 0)
            throw new ParseNomException(input, "Expected at least one whitespace");
        return new ParseSuccess<>(input.substring(i), input.substring(0, i));

    }

    // === MULTISPACE0 ===
    public static ParseSuccess<String> multispace0(String input) {
        int i = 0;
        while (i < input.length() && Character.isWhitespace(input.charAt(i))) {
            i++;
        }
        String matched = input.substring(0, i);
        String remaining = input.substring(i);
        return new ParseSuccess<>(remaining, matched);

    }

    // === DIGIT1 ===
    public static ParseSuccess<String> digit1(String input) throws ParseNomException {
        int i = 0;
        while (i < input.length() && Character.isDigit(input.charAt(i)))
            i++;
        if (i == 0)
            throw new ParseNomException(input, "Expected at least one digit");
        return new ParseSuccess<>(input.substring(i), input.substring(0, i));

    }

    // === DIGIT0 ===
    public static ParseSuccess<String> digit0(String input) {
        int i = 0;
        while (i < input.length() && Character.isDigit(input.charAt(i)))
            i++;
        return new ParseSuccess<>(input.substring(i), input.substring(0, i));
    }

    // === OPT ===
    public static <T> ParseSuccess<T> opt(ParserNom<T> parser, String input) {
        try {
            return parser.apply(input);
        } catch (ParseNomException e) {
            return new ParseSuccess<>(input, null);
        }
    }

    public static <T> ParserNom<T> optParser(ParserNom<T> parser) {
        return input -> {
            return opt(parser, input);
        };
    }

    // === DECIMAL1 ===
    public static ParseSuccess<Double> decimal1(String input) throws ParseNomException {
        ParseSuccess<String> intPartRes = ParserNomUtilHelper.parseIntegerPart(input);
        ParseSuccess<String> fracPartRes = ParserNomUtilHelper.parseFractionPart(intPartRes.remaining());
        return ParserNomUtilHelper.combineIntegerAndFraction(intPartRes, fracPartRes);

    }

    // === IDENTIFIER1 ===
    public static ParseSuccess<QualifiedIdentifier> identifier1(String input) throws ParseNomException {
        ParseSuccess<String> originPartRes = ParserNomUtilHelper.parseOriginPart(input);
        ParseSuccess<String> simpleNamePartRes = ParserNomUtilHelper.parseSimpleNamePart(originPartRes.remaining());
        return ParserNomUtilHelper.combineOriginAndName(originPartRes, simpleNamePartRes);

    }

    // === TAKEWHILE1 ===
    public static ParseSuccess<String> takeWhile1(java.util.function.Function<Character, Boolean> predicate,
            String input) throws ParseNomException {
        int i = 0;
        while (i < input.length() && predicate.apply(input.charAt(i)))
            i++;
        if (i == 0)
            throw new ParseNomException(input, "Expected at least one matching character");
        return new ParseSuccess<>(input.substring(i), input.substring(0, i));
    }

    public static ParserNom<String> takeWhile1(java.util.function.Function<Character, Boolean> predicate) {
        return input -> {
            return takeWhile1(predicate, input);
        };
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
    public static <T> ParserNom<List<T>> tuple(boolean trimMod, ParserNom<T>... parsers) {
        return input -> {
            List<T> results = new ArrayList<>();
            String rest = input;
            if (trimMod) {
                rest = rest.trim();
            }
            for (ParserNom<T> parser : parsers) {
                ParseSuccess<T> result = parser.apply(rest);
                results.add(result.matched());
                if (trimMod) {
                    rest = result.remaining().trim();

                } else {
                    rest = result.remaining();

                }
            }

            return new ParseSuccess<>(rest, results);
        };
    }

    // Fonction générique pour parser une liste entre parenthèses
    public static <T> ParserNom<Vector<T>> parseListBetweenParentheses(
            ParserNom<T> itemParser,
            String itemDescription) throws ParseNomException {

        return input -> {
            Vector<T> items = new Vector<>();
            String remaining = input.trim();

            // Vérifier la parenthèse ouvrante
            if (!remaining.startsWith("(")) {
                throw new ParseNomException(remaining, "Parenthèse ouvrante '(' attendue");
            }

            // Consommer '('
            remaining = remaining.substring(1).trim();

            // Si la liste est vide
            if (remaining.startsWith(")")) {
                remaining = remaining.substring(1).trim();
                return new ParseSuccess<>(remaining, items);
            }

            // Parser le premier élément
            ParseSuccess<T> firstItem = itemParser.apply(remaining);
            items.add(firstItem.matched());
            remaining = firstItem.remaining().trim();

            // Parser les éléments suivants
            while (true) {
                if (remaining.startsWith(",")) {
                    // Consommer la virgule
                    remaining = remaining.substring(1).trim();

                    if (remaining.startsWith(")")) {
                        throw new ParseNomException(remaining, itemDescription + " attendu après la virgule");
                    }

                    // Parser l'élément suivant
                    ParseSuccess<T> nextItem = itemParser.apply(remaining);
                    items.add(nextItem.matched());
                    remaining = nextItem.remaining().trim();
                } else if (remaining.startsWith(")")) {
                    // Fin de la liste
                    remaining = remaining.substring(1).trim();
                    break;
                } else {
                    throw new ParseNomException(remaining, "Virgule ',' ou parenthèse fermante ')' attendue");
                }
            }

            return new ParseSuccess<>(remaining, items);
        };
    }
}
