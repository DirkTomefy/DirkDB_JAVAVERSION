package sqlTsinjo.query.main.insert;

import java.io.IOException;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.base.err.RelationalErr;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.insert.element.abstracts.InsertRqstValues;
import sqlTsinjo.query.main.insert.element.classes.InsertRqstDefaultValues;
import sqlTsinjo.query.main.insert.token.InsertRqstTokenizer;
import sqlTsinjo.query.main.select.SelectRqst;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeRelation;

public class InsertRqst {
    public InsertRqst(String table, Vector<String> fieldName, InsertRqstValues values) {
        this.table = table;
        this.fieldName = fieldName;
        this.values = values;
    }

    String table;
    Vector<String> fieldName;
    InsertRqstValues values;

    public static ParseSuccess<Vector<String>> parseFieldName(String input) throws ParseNomException {
        String trimmed = input.trim();
        ParseSuccess<Vector<String>> result = ParserNomUtil.parseListBetweenParentheses(
                ParserNomUtil::tagName,
                "nom de champ").apply(trimmed);

        return result;
    }

    public void eval(AppContext context) throws IOException, ParseNomException, RelationalErr {
        SerdeRelation serde = new SerdeRelation(context, table);
        Relation rel = serde.deserializeRelation();
        rel.insert(fieldName, values,context);
        System.out.println(" "+this);
        System.out.println(" "+rel );
        serde.serializeRelation(rel);
    }

    public static ParseSuccess<InsertRqstValues> parseRealValues(String input) throws ParseNomException {
        ParseSuccess<Token> t = ParserNomUtil.optParser(InsertRqstTokenizer::scanValuesToken).apply(input.trim());
        if (t.matched() != null) {
            ParseSuccess<Vector<Vector<Object>>> defaultValues = InsertRqstDefaultValues
                    .parseMultipleValuesRows(t.remaining());
            return new ParseSuccess<InsertRqstValues>(defaultValues.remaining(),
                    new InsertRqstDefaultValues(defaultValues.matched()));
        } else {
            ParseSuccess<SelectRqst> selectrqst = SelectRqst.parseSelect(input);
            return new ParseSuccess<>(selectrqst.remaining(), selectrqst.matched());
        }
    }

    public static ParseSuccess<InsertRqst> parseInsert(String input) throws ParseNomException {
        ParseSuccess<Token> scanKeyWord = InsertRqstTokenizer.scanRealInsertToken(input.trim());
        ParseSuccess<String> tableName = ParserNomUtil.tagName(scanKeyWord.remaining());
        ParseSuccess<Vector<String>> fieldName = parseFieldName(tableName.remaining());
        ParseSuccess<InsertRqstValues> values = parseRealValues(fieldName.remaining());

        return new ParseSuccess<InsertRqst>(values.remaining(),
                new InsertRqst(tableName.matched(), fieldName.matched(), values.matched()));
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public InsertRqstValues getValues() {
        return values;
    }

    public Vector<String> getFieldName() {
        return fieldName;
    }

    public void setFieldName(Vector<String> fieldName) {
        this.fieldName = fieldName;
    }

    public void setValues(InsertRqstValues values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "InsertRqst [table=" + table + ", fieldName=" + fieldName + ", values=" + values + "]";
    }
}
