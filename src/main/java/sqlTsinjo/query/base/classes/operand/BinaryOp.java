package sqlTsinjo.query.base.classes.operand;
import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface BinaryOp {
    public Object applyByCtx(Relation relation,  Vector<Object> row, Expression left, Expression right, SelectCtx ctx) throws EvalErr;

}
