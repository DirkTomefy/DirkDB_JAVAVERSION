package query.main.select;

import java.util.LinkedHashMap;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import cli.AppContext;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.err.eval.AmbigousAliasErr;
import query.main.select.element.abstracts.SelectFields;
import query.main.select.element.abstracts.TableOriginWithAlias;
import query.main.select.element.classes.AllField;
import query.main.select.element.classes.JoinElement;
import query.main.select.element.classes.SelectCtx;
import query.main.select.token.SelectTokenizer;
import query.token.Token;

public class SelectRqst extends TableOriginWithAlias implements SelectExpr {
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

    public SelectCtx makeSelectCtx(AppContext context) throws AmbigousAliasErr {
        LinkedHashMap<String, String> aliasMap = new LinkedHashMap<>();
        if (from != null) {
            from.makeAliasAsTableOrigin(aliasMap);
        }
        for (JoinElement joinElement : joins) {
            joinElement.getTableOrigin().makeAliasAsTableOrigin(aliasMap);
        }
        return new SelectCtx(aliasMap, context);
    }

    @Override
    public Relation eval(AppContext context) throws ParseNomException, EvalErr {
        Relation result = null;
        SelectCtx selectCtx = makeSelectCtx(context);
        if (from == null) {
            result = Relation.makeDualRelation();
        } else {
            result = from.evalAsTableOriginAndHandleId(selectCtx);
        }
        if (joins != null)
            result = evalJoins(result, selectCtx);
        if (where != null)
            result = result.selection(where, selectCtx);
        result = result.projection(fields, selectCtx);
        return result;
    }

    public Relation evalJoins(Relation fromRelation, SelectCtx ctx) throws ParseNomException, EvalErr {
        Relation lastResult = fromRelation;
        if (joins != null) {
            for (JoinElement joinElement : joins) {
                lastResult = joinElement.evalJoinElement(lastResult, ctx);
            }
        }
        return lastResult;
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

    @Override
    public String toString() {
        return "SelectRqst [fields=" + fields + ", from=" + from + ", joins=" + joins + ", where=" + where + "]";
    }

    @Override
    public Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException, EvalErr {
        return this.eval(context.getAppcontext());
    }

    @Override
    public void makeAliasAsTableOrigin(LinkedHashMap<String, String> aliasMap) throws AmbigousAliasErr {
        if (aliasMap.containsKey(alias)) {
            throw new AmbigousAliasErr("Alias '" + alias + "' déjà utilisé");
        } else {
            aliasMap.put(alias, id);
        }
    }

}
