package query.base.classes.operand.other;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import query.base.classes.expr.Expression;
import query.base.classes.operand.BinaryOp;

public enum LogicalOp implements BinaryOp {
    AND,
    OR;

    @Override
    public Object applyByCtx(Relation relation,  Vector<Object> row,Expression left, Expression right) throws EvalErr {
        return switch (this) {
            case AND -> evalAnd(relation,row,left, right);
            case OR -> evalOr(relation,row,left, right);
        };
    }

    private Object evalAnd(Relation relation, Vector<Object> row,Expression left, Expression right) throws EvalErr {
        Object leftValue = left.eval(relation,row);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (!leftBool) {
            return false;
        }

        Object rightValue = right.eval(relation,row);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }

    private Object evalOr(Relation relation, Vector<Object> row, Expression left, Expression right) throws EvalErr {
      
        Object leftValue = left.eval(relation,row);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (leftBool) {
            return true;
        }
        Object rightValue = right.eval(relation,row);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }
}