package RDP.base.function.operand;
import java.util.Vector;

import RDP.base.function.expr.Expression;
import RDP.err.EvalErr;
 
import base.Relation;

public interface BinaryOp {
    public Object applyByCtx(Relation relation,  Vector<Object> row, Expression left, Expression right) throws EvalErr;

}
