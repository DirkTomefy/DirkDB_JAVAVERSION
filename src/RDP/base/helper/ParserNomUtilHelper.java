package RDP.base.helper;

import java.util.function.Function;

import RDP.base.ParseSuccess;
import RDP.err.ParseNomException;

public class ParserNomUtilHelper {

    // Parse la partie entière (au moins un chiffre)
    public static ParseSuccess<String> parseIntegerPart(String input) throws ParseNomException {
        return ParserNomUtil.digit1().apply(input);
    }

    // Parse la partie fractionnaire optionnelle (commence par un point)
    public static ParseSuccess<String> parseFractionPart(String input) throws ParseNomException {
        return ParserNomUtil.opt(inp -> {
            if (!inp.startsWith(".")) return new ParseSuccess<>(inp, null);
            ParseSuccess<String> digits = ParserNomUtil.digit1().apply(inp.substring(1));
            String frac = "." + digits.matched();
            return new ParseSuccess<>(digits.remaining(), frac);
        }, input);
    }

    // Combine partie entière et fractionnaire en Double
    public static ParseSuccess<Double> combineIntegerAndFraction(ParseSuccess<String> intPart,
            ParseSuccess<String> fracPart) throws ParseNomException {
        return combineFirstAndRest(intPart, fracPart, Double::parseDouble);
    }

    // Vérifie si le caractère peut démarrer un identifiant
    public static boolean isIdentStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    // Vérifie si le caractère peut faire partie d’un identifiant
    public static boolean isIdentChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    // Parse le premier caractère d’un identifiant
    public static ParseSuccess<String> parseFirstChar(String input) throws ParseNomException {
        return ParserNomUtil.takeWhile1(ParserNomUtilHelper::isIdentStart, input);
    }

    // Parse le reste des caractères d’un identifiant
    public static ParseSuccess<String> parseRestChars(String input) throws ParseNomException {
        return ParserNomUtil.opt(inp -> ParserNomUtil.takeWhile1(ParserNomUtilHelper::isIdentChar, inp), input);
    }

    // Combine deux ParseSuccess avec une fonction de transformation
    private static <T, R> ParseSuccess<R> combineFirstAndRest(
            ParseSuccess<T> first,
            ParseSuccess<T> rest,
            Function<String, R> mapper) throws ParseNomException {

        String firstStr = first.matched() != null ? first.matched().toString() : "";
        String restStr = rest.matched() != null ? rest.matched().toString() : "";
        String combined = firstStr + restStr;

        R value;
        try {
            value = mapper.apply(combined);
        } catch (Exception e) {
            throw new ParseNomException(rest.remaining(), "Failed to map combined value");
        }

        return new ParseSuccess<>(rest.remaining(), value);
    }

    // Spécialisation pour les tags (retourne une String combinée)
    public static ParseSuccess<String> combineFirstAndRestForTag(ParseSuccess<String> first, ParseSuccess<String> rest)
            throws ParseNomException {
        return combineFirstAndRest(first, rest, s -> s);
    }
}
