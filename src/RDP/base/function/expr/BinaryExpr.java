package RDP.base.function.expr;
import java.util.Vector;

import RDP.base.function.operand.BinaryOp;
import RDP.err.EvalErr;
 
import base.Relation;

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
    public Object eval(Relation relation,  Vector<Object> row) throws EvalErr {
        return this.op.applyByCtx(relation,row, left, right);
    }
}
