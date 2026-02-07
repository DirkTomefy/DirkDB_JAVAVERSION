package sqlTsinjo.query.main.select.element.classes;

import java.io.IOException;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.select.element.abstracts.TableOriginWithAlias;
import sqlTsinjo.query.main.select.element.enums.JoinOp;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

public class JoinElement {
    JoinOp op;
    TableOriginWithAlias tableOrigin;
    Expression onCondition;

    public JoinElement() {
        // Default constructor for Jackson
    }

    public JoinElement(JoinOp op, TableOriginWithAlias table, Expression onCondition) {
        this.op = op;
        this.tableOrigin = table;
        this.onCondition = onCondition;
    }

    public void setOp(JoinOp op) {
        this.op = op;
    }

    public void setTableOrigin(TableOriginWithAlias tableOrigin) {
        this.tableOrigin = tableOrigin;
    }

    public void setOnCondition(Expression onCondition) {
        this.onCondition = onCondition;
    }

    public static ParseSuccess<Vector<JoinElement>> parseJoins(String input) throws ParseNomException {
        Vector<JoinElement> maybeResult = new Vector<>();
        while (!Tokenizer.codonStop(input)) {
            ParseSuccess<Token> tokenSuccess = ParserNomUtil.opt(SelectTokenizer::scanJoinsToken, input);
            if (tokenSuccess.matched() == null) {
                break;
            } else {
                ParseSuccess<JoinElement> joinElement = parseSingleJoin(input);
                maybeResult.add(joinElement.matched());
                input = joinElement.remaining();
            }
        }
        return new ParseSuccess<>(input, maybeResult);
    }

    public static ParseSuccess<JoinElement> parseSingleJoin(String input) throws ParseNomException {
        ParseSuccess<Token> tokenSuccess = SelectTokenizer.scanJoinsToken(input);
        ParseSuccess<TableOriginWithAlias> tableSuccess = TableOriginWithAlias
                .parseTableOrigin(tokenSuccess.remaining());
        ParseSuccess<Expression> onConditionSuccess = parseOptionalOnCondition(tableSuccess.remaining());
        // INITIALISATION DES ATTRIBUT :
        JoinOp op = (JoinOp) tokenSuccess.matched().value;
        TableOriginWithAlias table = tableSuccess.matched();
        Expression joinCondition = onConditionSuccess.matched();
        return new ParseSuccess<JoinElement>(onConditionSuccess.remaining(), new JoinElement(op, table, joinCondition));
    }

    public static ParseSuccess<Expression> parseOptionalOnCondition(String input) throws ParseNomException {
        ParseSuccess<String> onCondition = ParserNomUtil.opt(ParserNomUtil.tagNoCase("#ka"), input.trim());
        if (onCondition.matched() != null) {
            return Expression.parseExpression.apply(onCondition.remaining());
        } else {
            return new ParseSuccess<>(input, null);
        }
    }

    public Relation evalJoinElement(Relation src, SelectCtx ctx) throws ParseNomException, EvalErr, IOException {
        Relation result = tableOrigin.evalAsTableOriginAndHandleId(ctx);
        switch (op) {
            case FULL:
                result = src.jointureExternePleine(result, onCondition, ctx);
                break;
            case INNER:
                result = src.jointureInterne(result, onCondition, ctx);
                break;
            case LEFT:
                result = src.jointureExterneGauche(result, onCondition, ctx);
                break;
            case NATURAL:
                result = src.jointureNaturelle(result, onCondition, ctx);
                break;
            case RIGHT:
                result = src.jointureExterneDroite(result, onCondition, ctx);
                break;
            default:
                result = src.jointureInterne(result, onCondition, ctx);
                break;

        }

        return result;
    }

    @Override
    public String toString() {
        return "JoinElement [op=" + op + ", table=" + tableOrigin + ", onCondition=" + onCondition + "]";
    }

    public JoinOp getOp() {
        return op;
    }

    public TableOriginWithAlias getTableOrigin() {
        return tableOrigin;
    }

    public Expression getOnCondition() {
        return onCondition;
    }
}
