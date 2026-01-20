package sqlTsinjo.query.base.classes.expr.helper;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.classes.expr.FunctionExpr;
import sqlTsinjo.query.base.classes.expr.PrefixedExpr;
import sqlTsinjo.query.base.classes.expr.PrimitiveExpr;
import sqlTsinjo.query.base.classes.operand.PrefixedOp;
import sqlTsinjo.query.base.classes.operand.other.ArithmeticOp;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

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

    public static ParseSuccess<Expression> handleId(Token t, String rest) throws ParseNomException {
        QualifiedIdentifier val = (QualifiedIdentifier) t.getValue();
        String remaining = rest;

        // Détection d'appel de fonction: fn(...)
        // On n'autorise que les fonctions sans origine (pas de "t1.fn(...)" pour éviter la confusion)
        if (val.getOrigin() == null) {
            String trimmed = remaining.trim();
            if (trimmed.startsWith("(")) {
                ParseSuccess<java.util.Vector<Expression>> args = ParserNomUtil
                        .parseListBetweenParentheses(Expression.parseExpression, "Expression")
                        .apply(trimmed);
                return new ParseSuccess<>(args.remaining(), new FunctionExpr(val.getName(), args.matched()));
            }
        }

        return new ParseSuccess<>(remaining, PrimitiveExpr.id(val));
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
