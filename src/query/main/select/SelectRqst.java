package query.main.select;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.main.select.element.interfaces.SelectFields;
import query.main.select.element.interfaces.TableOrigin;
import query.main.select.token.SelectTokenizer;
import query.token.Token;

public class SelectRqst implements TableOrigin {
    SelectFields fields;
    TableOrigin from;
    Expression where;

    public SelectRqst(SelectFields fields, TableOrigin from, Expression where) {
        this.fields = fields;
        this.from = from;
        this.where = where;
    }

    public static SelectRqst parse(String input) throws ParseNomException{
        ParseSuccess<Token> token=SelectTokenizer.scanSelectToken(input);
        ParseSuccess<SelectFields> fields=SelectFields.parse(input);
        return null;
    }
}
