package query.main.select;

import java.util.Vector;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.main.select.element.abstracts.SelectFields;
import query.main.select.element.abstracts.TableOriginWithAlias;
import query.main.select.element.classes.AllField;
import query.main.select.element.classes.JoinElement;
import query.main.select.token.SelectTokenizer;
import query.token.Token;

public class SelectRqst extends TableOriginWithAlias {
    SelectFields fields;
    TableOriginWithAlias from;
    Vector<JoinElement> joins;
    Expression where;

    public SelectRqst(SelectFields fields, TableOriginWithAlias from, Vector<JoinElement> joins, Expression where) {
        this.fields = fields;
        this.from = from;
        this.joins = joins;
        this.where = where;
    }

    public static ParseSuccess<SelectRqst> parseSelect(String input) throws ParseNomException {
        ParseSuccess<Token> token = SelectTokenizer.scanSelectToken(input);
        ParseSuccess<SelectFields> fields = SelectFields.parse(token.remaining());
        ParseSuccess<TableOriginWithAlias> from = parseOptionalTableOrigin(fields.remaining());
        ParseSuccess<Vector<JoinElement>> joins = JoinElement.parseJoins(from.remaining());

        if (fields.matched() instanceof AllField && from.matched() == null) {
            throw new ParseNomException(input,
                    "Vous ne pouvez pas faire select all si l'origin de la table n'existe pas");
        }
        ParseSuccess<Expression> where = parseOptionalWhere(joins.remaining());
        return new ParseSuccess<SelectRqst>(where.remaining(),
                new SelectRqst(fields.matched(), from.matched(), joins.matched(), where.matched()));
    }

    public static ParseSuccess<TableOriginWithAlias> parseOptionalTableOrigin(String input) throws ParseNomException {
        ParseSuccess<Token> fromToken = ParserNomUtil.opt(inp -> {
            return SelectTokenizer.scanFromToken(inp);
        }, input);
        if (fromToken.matched() == null) {
            return new ParseSuccess<TableOriginWithAlias>(input, null);
        } else {
            return TableOriginWithAlias.parseTableOrigin(fromToken.remaining());
        }

    }

    public static ParseSuccess<Expression> parseOptionalWhere(String input) throws ParseNomException {
        ParseSuccess<Token> whereToken = ParserNomUtil.opt(inp -> {
            return SelectTokenizer.scanWhereToken(inp);
        }, input);
        if (whereToken.matched() == null) {
            return new ParseSuccess<Expression>(input, null);
        } else {
            return Expression.parseExpression.apply(whereToken.remaining());
        }
    }

    // TODO mettre une fonction eval

    @Override
    public String toString() {
        return "SelectRqst [fields=" + fields + ", from=" + from + ", joins=" + joins + ", where=" + where + "]";
    }

}
