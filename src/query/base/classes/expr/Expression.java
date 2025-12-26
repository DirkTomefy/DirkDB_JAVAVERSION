package query.base.classes.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.ParserNom;
import query.base.classes.expr.helper.FactorHelper;
import query.base.classes.expr.helper.SpecialBinOpHandler;
import query.base.classes.operand.BinaryOp;
import query.base.classes.operand.other.ArithmeticOp;
import query.base.classes.operand.other.CompareOp;
import query.base.classes.operand.other.LogicalOp;
import query.base.helper.ParserNomUtil;
import query.err.parsing.AfterIsOrIsNotErr;
import query.main.select.element.classes.SelectCtx;
import query.token.Token;
import query.token.TokenKind;
import query.token.Tokenizer;

public interface Expression {

    // ========================= PARSEUR =========================
    ParserNom<Expression> parseExpression = input -> parseExprLevel(0, input);

    BinaryOp[][] LEVELS = {
            { LogicalOp.OR, LogicalOp.AND },
            CompareOp.allCompareOp(),
            { ArithmeticOp.ADD, ArithmeticOp.MIN },
            { ArithmeticOp.MUL, ArithmeticOp.DIV }
    };

    Map<BinaryOp, SpecialBinOpHandler> BINOP_HANDLER = initHandler();
    
    Object eval(Relation relation, Vector<Object> row) throws EvalErr;

    
    //TODO REMPLIR CETTE FONCTION :
    Object evalByCtx(Relation relation, Vector<Object> row, SelectCtx ctx) throws EvalErr;
    
    default boolean evalToBoolean(Relation relation, Vector<Object> row) throws EvalErr {
        return ObjectIntoBoolean(eval(relation, row));
    }

    static boolean ObjectIntoBoolean(Object e) {
        if (e == null)
            return false;
        if (e instanceof Boolean b)
            return b;
        if (e instanceof Number n)
            return n.doubleValue() != 0.0;
        if (e instanceof String s) {
            String str = s.trim().toLowerCase();
            return str.equals("true") || str.equals("1") || str.equals("yes") ||
                    str.equals("on") || str.equals("t") || str.equals("y");
        }
        return true;
    }

    static double booleanIntoDouble(boolean b) {
        return b ? 1.0 : 0.0;
    }

    static boolean containsOp(BinaryOp[] list, BinaryOp value) {
        for (BinaryOp item : list)
            if (value.equals(item))
                return true;
        return false;
    }

    // ================== Parseur générique de niveau ==================
    static ParseSuccess<Expression> parseExprLevel(int level, String input) throws ParseNomException {
        if (level >= LEVELS.length)
            return parseFactor0(input);

        ParseSuccess<Expression> result = parseExprLevel(level + 1, input);
        input = result.remaining();
        Expression current = result.matched();

        while (true) {
            if (Tokenizer.codonStop(input))
                break;

            String oldInput = input;
            ParseSuccess<Token> next = ParserNomUtil.opt(Tokenizer::scanBinopToken, input);
                if (next.matched() == null)
                    break;

                Token token = next.matched();
                input = next.remaining();

                if (token.status == TokenKind.BINOP) {
                    BinaryOp op = (BinaryOp) token.getValue();

                    if (BINOP_HANDLER.containsKey(op)) {
                        SpecialBinOpHandler handler = BINOP_HANDLER.get(op);
                        ParseSuccess<Expression> special = handler.handle(current, input);
                        input = special.remaining();
                        current = special.matched();
                        continue;
                    }

                    if (containsOp(LEVELS[level], op)) {
                        ParseSuccess<Expression> rhs = parseExprLevel(level + 1, input);
                        input = rhs.remaining();
                        current = new BinaryExpr(current, op, rhs.matched());
                        continue;
                    } else {
                        input = oldInput;
                    }
                } else {
                    input = oldInput;
                }
                break;
            }
        return new ParseSuccess<>(input, current);
    }

    // ================== Parseur de facteur ==================
    static ParseSuccess<Expression> parseFactor0(String input) throws ParseNomException {
        ParseSuccess<Token> tSuccess = Tokenizer.scanFactorToken(input);
        Token t = tSuccess.matched();
        String rest = tSuccess.remaining();

        return switch (t.status) {
            case NUMBER -> FactorHelper.handleNumber(t, rest);
            case OTHER -> FactorHelper.handleParens(t, rest, input);
            case ID -> FactorHelper.handleId(t, rest);
            case PREFIXEDOP -> FactorHelper.handlePrefixedOp(t, rest);
            case BINOP -> FactorHelper.handleBinOp(t, rest, input);
            case NULLVALUE -> new ParseSuccess<>(rest, new PrimitiveExpr(PrimitiveKind.NULLVALUE, null));
            case STRING -> FactorHelper.handleString(t, rest);
            default -> throw ParseNomException.buildTokenWrongPlace(t, input);
        };
    }

    // ================== Handler spécial ==================
    private static SpecialBinOpHandler makeHandlerIs_IsNot(CompareOp value) {
        return (left, input) -> {
            ParseSuccess<Token> next = Tokenizer.scanFactorToken(input);
            Token t = next.matched();
            if (t.status != TokenKind.NULLVALUE)
                throw new AfterIsOrIsNotErr(input);

            Expression right = new PrimitiveExpr(PrimitiveKind.NULLVALUE, null);
            return new ParseSuccess<>(next.remaining(), new BinaryExpr(left, value, right));
        };
    }

    private static Map<BinaryOp, SpecialBinOpHandler> initHandler() {
        Map<BinaryOp, SpecialBinOpHandler> handlers = new HashMap<>();
        handlers.put(CompareOp.Is, makeHandlerIs_IsNot(CompareOp.Is));
        handlers.put(CompareOp.IsNot, makeHandlerIs_IsNot(CompareOp.IsNot));
        return handlers;
    }
}
