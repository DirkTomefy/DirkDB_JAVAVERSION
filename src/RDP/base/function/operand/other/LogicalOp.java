package RDP.base.function.operand.other;
import RDP.base.function.expr.Expression;
import RDP.base.function.operand.BinaryOp;
import RDP.err.EvalErr;
import base.Individual;
import base.Relation;

public enum LogicalOp implements BinaryOp {
    AND,
    OR;

    @Override
    public Object applyByCtx(Relation relation, Individual row,Expression left, Expression right) throws EvalErr {
        return switch (this) {
            case AND -> evalAnd(relation,row,left, right);
            case OR -> evalOr(relation,row,left, right);
        };
    }

    private Object evalAnd(Relation relation,Individual row,Expression left, Expression right) throws EvalErr {
        Object leftValue = left.eval(relation,row);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (!leftBool) {
            return false;
        }

        Object rightValue = right.eval(relation,row);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }

    private Object evalOr(Relation relation,Individual row, Expression left, Expression right) throws EvalErr {
      
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