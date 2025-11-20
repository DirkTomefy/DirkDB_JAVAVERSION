package RDP.base.function.expr;

import java.util.Vector;

import RDP.base.function.operand.BinaryOp;
import RDP.err.EvalErr;
import base.Individual;

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
    public Object eval(Individual row, Vector<String> fieldName) throws EvalErr {
        return this.op.applyByCtx(row, fieldName, left, right);
    }
}
