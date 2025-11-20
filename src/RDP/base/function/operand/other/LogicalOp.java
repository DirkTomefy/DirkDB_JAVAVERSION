package RDP.base.function.operand.other;

import java.util.Vector;

import RDP.base.function.expr.Expression;
import RDP.base.function.operand.BinaryOp;
import RDP.err.EvalErr;
import base.Individual;

public enum LogicalOp implements BinaryOp {
    AND,
    OR;

    @Override
    public Object applyByCtx(Individual row, Vector<String> fieldName, Expression left, Expression right) throws EvalErr {
        return switch (this) {
            case AND -> evalAnd(row, fieldName, left, right);
            case OR -> evalOr(row, fieldName, left, right);
        };
    }

    private Object evalAnd(Individual row, Vector<String> fieldName, Expression left, Expression right) throws EvalErr {
        Object leftValue = left.eval(row, fieldName);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (!leftBool) {
            return false;
        }

        Object rightValue = right.eval(row, fieldName);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }

    private Object evalOr(Individual row, Vector<String> fieldName, Expression left, Expression right) throws EvalErr {
      
        Object leftValue = left.eval(row, fieldName);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (leftBool) {
            return true;
        }
        Object rightValue = right.eval(row, fieldName);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }
}