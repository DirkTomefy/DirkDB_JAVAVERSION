package sqlTsinjo.query.base.classes.expr;
import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.base.classes.operand.BinaryOp;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class BinaryExpr implements Expression {
    private Expression left;
    private BinaryOp op;
    private Expression right;

    public BinaryExpr() {
        // Default constructor for Jackson
    }

    public BinaryExpr(Expression left, BinaryOp op, Expression right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public BinaryOp getOp() {
        return op;
    }

    public void setOp(BinaryOp op) {
        this.op = op;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
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
