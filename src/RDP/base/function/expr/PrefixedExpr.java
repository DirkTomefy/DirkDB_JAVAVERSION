package RDP.base.function.expr;

import RDP.base.function.operand.PrefixedOp;
import RDP.err.EvalErr;
import base.Individual;
import base.Relation;

public class PrefixedExpr implements Expression {
    private PrefixedOp op;
    private Expression expr;

    public PrefixedExpr(PrefixedOp op, Expression expr) {
        this.op = op;
        this.expr = expr;
    }

  

    @Override
    public String toString() {
        return op + "(" + expr.toString() + ")";
    }

    public PrefixedOp getOp() {
        return op;
    }

    public void setOp(PrefixedOp op) {
        this.op = op;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }



    @Override
    public Object eval(Relation relation,Individual row) throws EvalErr {
        Object value = expr.eval(relation,row);
        
        System.out.println(""+expr.toString());

        return switch (op) {
            case NOT -> evalNot(value);
            case NEG -> evalNeg(value);
        };
    }

    private Object evalNot(Object value) throws EvalErr {
        boolean boolValue = Expression.ObjectIntoBoolean(value);
        return !boolValue;
    }

    private Object evalNeg(Object value) throws EvalErr {
        double doubleValue;
        
        if (value instanceof Number) {
            doubleValue = ((Number) value).doubleValue();
        } else {
            boolean boolValue = Expression.ObjectIntoBoolean(value);
            doubleValue = Expression.booleanIntoDouble(boolValue);
        }
        
        return -doubleValue;
    }



}
