package query.base.classes.expr;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import query.base.classes.operand.BinaryOp;
import query.main.select.element.classes.SelectCtx;

public class BinaryExpr implements Expression {
    private final Expression left;
    private final BinaryOp op;
    private final Expression right;

    public BinaryExpr(Expression left, BinaryOp op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }



    @Override
    public Object eval(Relation relation,  Vector<Object> row, SelectCtx ctx) throws EvalErr {
        return this.op.applyByCtx(relation,row, left, right,ctx);
    }
}
