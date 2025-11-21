package RDP.base.function.operand;
import RDP.base.function.expr.Expression;
import RDP.err.EvalErr;
import base.Individual;
import base.Relation;

public interface BinaryOp {
    public Object applyByCtx(Relation relation, Individual row, Expression left, Expression right) throws EvalErr;

}
