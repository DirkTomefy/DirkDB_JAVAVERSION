package RDP.base.function.operand;
import java.util.Vector;

import RDP.base.function.expr.Expression;
import base.Relation;
import base.err.EvalErr;

public interface BinaryOp {
    public Object applyByCtx(Relation relation,  Vector<Object> row, Expression left, Expression right) throws EvalErr;

}
