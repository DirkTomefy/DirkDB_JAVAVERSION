package query.base.function.operand.other;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.function.expr.Expression;
import query.base.function.operand.BinaryOp;
import query.base.helper.ParserNomUtil;
import query.err.eval.DivisionByZeroErr;

public enum ArithmeticOp implements BinaryOp {
    ADD,
    MIN,
    MUL,
    DIV;

    @Override
    public Object applyByCtx(Relation relation, Vector<Object> row, Expression left, Expression right)
            throws EvalErr {
        Object leftValue = left.eval(relation,row);
        Object rightValue = right.eval(relation,row);

        double leftDouble = toDouble(leftValue);
        double rightDouble = toDouble(rightValue);

        return applyOperation(leftDouble, rightDouble);
    }

    private static double toDouble(Object value)  {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }else if (value instanceof String s){
            try {
                ParseSuccess<Double> a=ParserNomUtil.decimal1().apply(s); 
                return a.matched();   
            } catch (ParseNomException e) {}
            
        }
        return makedefaultToDouble(value);
    }

    private static double makedefaultToDouble(Object value) {
        return Expression.booleanIntoDouble(Expression.ObjectIntoBoolean(value));
    }

    private double applyOperation(double left, double right) throws EvalErr {
        return switch (this) {
            case ADD -> left + right;
            case MIN -> left - right;
            case MUL -> left * right;
            case DIV -> {
                if (right == 0.0) {
                    throw new DivisionByZeroErr(left);
                }
                yield left / right;
            }
        };
    }
}
