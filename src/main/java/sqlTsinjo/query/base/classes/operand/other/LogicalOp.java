package sqlTsinjo.query.base.classes.operand.other;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.classes.operand.BinaryOp;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public enum LogicalOp implements BinaryOp {
    AND,
    OR;

    @Override
    public Object applyByCtx(Relation relation,  Vector<Object> row,Expression left, Expression right, SelectCtx ctx) throws EvalErr {
        return switch (this) {
            case AND -> evalAnd(relation,row,left, right,ctx);
            case OR -> evalOr(relation,row,left, right,ctx);
        };
    }

    private Object evalAnd(Relation relation, Vector<Object> row,Expression left, Expression right, SelectCtx ctx) throws EvalErr {
        Object leftValue = left.eval(relation,row,ctx);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (!leftBool) {
            return false;
        }

        Object rightValue = right.eval(relation,row,ctx);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }

    private Object evalOr(Relation relation, Vector<Object> row, Expression left, Expression right,SelectCtx ctx) throws EvalErr {
      
        Object leftValue = left.eval(relation,row,ctx);
        boolean leftBool = Expression.ObjectIntoBoolean(leftValue);
        if (leftBool) {
            return true;
        }
        Object rightValue = right.eval(relation,row,ctx);
        boolean rightBool = Expression.ObjectIntoBoolean(rightValue);
        
        return rightBool;
    }
}