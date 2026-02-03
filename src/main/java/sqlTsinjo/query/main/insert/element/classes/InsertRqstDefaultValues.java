package sqlTsinjo.query.main.insert.element.classes;

import java.util.Vector;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.insert.element.abstracts.InsertRqstValues;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

public class InsertRqstDefaultValues implements InsertRqstValues {
    Vector<Vector<Object>> values;

    public InsertRqstDefaultValues(Vector<Vector<Object>> values) {
        this.values = values;
    }

    public static ParseSuccess<Object> parseSingleValue(String input) throws ParseNomException {
        ParseSuccess<Token> t = ParserNomUtil.alt(
                Tokenizer.tagNumber(),
                Tokenizer.tagString(),
                Tokenizer.tagNullValue()).apply(input);

        return new ParseSuccess<>(t.remaining(), t.matched().value);
    }

    public static ParseSuccess<Vector<Object>> parseSingleValuesRow(String input) throws ParseNomException {
        return ParserNomUtil.parseListBetweenParentheses(InsertRqstDefaultValues::parseSingleValue, "values row ")
                .apply(input);
    }

    public static ParseSuccess<Vector<Vector<Object>>> parseMultipleValuesRows(String input) throws ParseNomException {
        Vector<Vector<Object>> allRows = new Vector<>();
        String remaining = input.trim();

        // Parser la première ligne
        ParseSuccess<Vector<Object>> firstRow = parseSingleValuesRow(remaining);
        allRows.add(firstRow.matched());
        remaining = firstRow.remaining().trim();

        // Parser les lignes suivantes séparées par des virgules
        while (remaining.startsWith(",")) {
            // Consommer la virgule
            remaining = remaining.substring(1).trim();

            if (remaining.isEmpty()) {
                throw new ParseNomException(remaining, "Ligne de valeurs attendue après la virgule");
            }

            // Parser la ligne suivante
            ParseSuccess<Vector<Object>> nextRow = parseSingleValuesRow(remaining);
            allRows.add(nextRow.matched());
            remaining = nextRow.remaining().trim();
        }

        return new ParseSuccess<>(remaining, allRows);
    }

    @Override
    public String toString() {
        return "InsertRqstDefaultValues [values=" + values + "]";
    }

    @Override
    public Vector<Vector<Object>> getMultiplyValues(AppContext context) {
        return this.values;
    }
}
