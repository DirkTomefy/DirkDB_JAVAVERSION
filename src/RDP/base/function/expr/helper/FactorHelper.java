package RDP.base.function.expr.helper;

import RDP.base.ParseSuccess;
import RDP.base.function.expr.Expression;
import RDP.base.function.expr.PrefixedExpr;
import RDP.base.function.expr.PrimitiveExpr;
import RDP.base.function.operand.PrefixedOp;
import RDP.base.function.operand.other.ArithmeticOp;
import RDP.token.Token;
import RDP.token.Tokenizer;
import base.err.ParseNomException;

public class FactorHelper {
    public static ParseSuccess<Expression> handleNumber(Token t, String rest) {
        Double val = (Double) t.getValue();
        return new ParseSuccess<>(rest, PrimitiveExpr.number(val));
    }

    public static ParseSuccess<Expression> handleString(Token t, String rest) {
        String val = (String) t.getValue();
        return new ParseSuccess<>(rest, PrimitiveExpr.string(val));
    }

    public static ParseSuccess<Expression> handleParens(Token t, String rest, String input) throws ParseNomException {
        Object val = t.getValue();
        if ("(".equals(val)) {
            ParseSuccess<Expression> exp = Expression.parseExpression.apply(rest);
            ParseSuccess<Token> next = Tokenizer.scanFactorToken(exp.remaining());

            if (")".equals(next.matched().getValue())) {
                return new ParseSuccess<>(next.remaining(), exp.matched());
            } else {
                throw ParseNomException.buildParensMissing(t, rest);
            }
        }

        throw ParseNomException.buildTokenWrongPlace(t, input);
    }

    public static ParseSuccess<Expression> handleId(Token t, String rest) {
        String val = (String) t.getValue();
        return new ParseSuccess<>(rest, PrimitiveExpr.id(val));
    }

    public static ParseSuccess<Expression> handlePrefixedOp(Token t, String rest) throws ParseNomException {
        ParseSuccess<Expression> exp = Expression.parseFactor0(rest);
        return new ParseSuccess<>(exp.remaining(), new PrefixedExpr((PrefixedOp) t.getValue(), exp.matched()));
    }

    public static ParseSuccess<Expression> handleBinOp(Token t,String rest,String input) throws ParseNomException {
        if (ArithmeticOp.MIN.equals(t.value)) {
            ParseSuccess<Expression> exp = Expression.parseFactor0(rest);
            return new ParseSuccess<>(exp.remaining(), new PrefixedExpr(PrefixedOp.NEG, exp.matched()));
        } else {
            throw new ParseNomException(input, t.value + " can not use as PrefixedOp ");
        }
    }
}
