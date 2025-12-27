package query.main.select.element.classes;

import java.util.Vector;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.main.select.element.abstracts.TableOriginWithAlias;
import query.main.select.element.enums.JoinOp;
import query.main.select.token.SelectTokenizer;
import query.token.Token;
import query.token.Tokenizer;

public class JoinElement {
    JoinOp op;
    TableOriginWithAlias table;
    Expression onCondition;

    public JoinElement(JoinOp op, TableOriginWithAlias table, Expression onCondition) {
        this.op = op;
        this.table = table;
        this.onCondition = onCondition;
    }

    public static ParseSuccess<Vector<JoinElement>> parseJoins(String input) throws ParseNomException {
        Vector<JoinElement> maybeResult=new Vector<>();
        while (!Tokenizer.codonStop(input)) {
            ParseSuccess<Token> tokenSuccess = ParserNomUtil.opt(SelectTokenizer::scanJoinsToken, input);
            if(tokenSuccess.matched()==null){
                break;
            }else{
                ParseSuccess<JoinElement> joinElement=parseSingleJoin(input);
                maybeResult.add(joinElement.matched());
                input=joinElement.remaining();
            }
        }
        return new ParseSuccess<>(input,maybeResult);
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

    @Override
    public String toString() {
        return "JoinElement [op=" + op + ", table=" + table + ", onCondition=" + onCondition + "]";
    }
}
