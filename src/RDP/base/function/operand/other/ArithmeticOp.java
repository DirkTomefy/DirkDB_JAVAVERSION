package RDP.base.function.operand.other;
import java.util.Vector;

import RDP.base.ParseSuccess;
import RDP.base.function.expr.Expression;
import RDP.base.function.operand.BinaryOp;
import RDP.base.helper.ParserNomUtil;
import RDP.err.EvalErr;
import RDP.err.ParseNomException;
import RDP.err.eval.DivisionByZeroErr;
 
import base.Relation;

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
