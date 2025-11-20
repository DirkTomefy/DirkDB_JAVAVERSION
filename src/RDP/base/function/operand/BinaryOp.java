package RDP.base.function.operand;


import java.util.Vector;

import RDP.base.function.expr.Expression;
import RDP.err.EvalErr;
import base.Individual;

public interface BinaryOp {
    public Object applyByCtx(Individual row, Vector<String> fieldName, Expression left, Expression right) throws EvalErr;

}
