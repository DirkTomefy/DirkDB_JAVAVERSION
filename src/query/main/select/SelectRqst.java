package query.main.select;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.main.select.element.err.AliasNeededException;
import query.main.select.element.interfaces.SelectFields;
import query.main.select.element.interfaces.TableOriginWithAlias;
import query.main.select.token.SelectTokenizer;
import query.token.Token;

public class SelectRqst extends TableOriginWithAlias {
    SelectFields fields;
    TableOriginWithAlias from;
    Expression where;

    public SelectRqst(SelectFields fields, TableOriginWithAlias from, Expression where) {
        this.fields = fields;
        this.from = from;
        this.where = where;
    }

    public static ParseSuccess<SelectRqst> parseSelect(String input) throws ParseNomException {
        ParseSuccess<Token> token = SelectTokenizer.scanSelectToken(input);
        ParseSuccess<SelectFields> fields = SelectFields.parse(token.remaining());
        ParseSuccess<TableOriginWithAlias> from = parseOptionalTableOrigin(fields.remaining());
        ParseSuccess<Expression> where = parseOptionalWhere(from.remaining());

        return new ParseSuccess<SelectRqst>(where.remaining(),
                new SelectRqst(fields.matched(), from.matched(), where.matched()));
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
            return SelectTokenizer.scanFromToken(inp);
        }, input);
        if (whereToken.matched() == null) {
            return new ParseSuccess<Expression>(input, null);
        } else {
            return Expression.parseExpression.apply(whereToken.remaining());
        }
    }
}
