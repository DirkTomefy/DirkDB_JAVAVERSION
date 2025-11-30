package query.base.classes.operand;
import java.util.Vector;

import base.Relation;
import base.err.EvalErr;
import query.base.classes.expr.Expression;

public interface BinaryOp {
    public Object applyByCtx(Relation relation,  Vector<Object> row, Expression left, Expression right) throws EvalErr;

}
