package RDP.base.function.operand.other;
import RDP.base.function.expr.Expression;
import RDP.base.function.expr.PrimitiveExpr;
import RDP.base.function.expr.PrimitiveKind;
import RDP.base.function.operand.BinaryOp;
import RDP.err.EvalErr;
import RDP.err.eval.InvalidArgumentErr;
import base.Individual;
import base.Relation;

public enum CompareOp implements BinaryOp {
    Eq,
    Neq,
    Lt,
    Lte,
    Gt,
    Gte,
    Is,
    IsNot;

    public static CompareOp[] allCompareOp() {
        return new CompareOp[] { CompareOp.Eq, CompareOp.Neq, CompareOp.Lt, CompareOp.Lte, CompareOp.Gt,
                CompareOp.Gte, CompareOp.Is, CompareOp.IsNot };
    }

    @Override
    public Object applyByCtx(Relation relation,Individual row, Expression left, Expression right)
            throws EvalErr {
        return switch (this) {
            case Is, IsNot -> evalIsComparison(relation, row, left, right);
            default -> evalStandardComparison(relation, row, left, right);
        };
    }

    private Object evalIsComparison(Relation relation,Individual row, Expression left, Expression right)
            throws EvalErr {
        if (!(right instanceof PrimitiveExpr)) {
            throw new InvalidArgumentErr("IS/IS NOT", "right operand must be a primitive null value");
        }

        PrimitiveExpr primitiveRight = (PrimitiveExpr) right;
        if (primitiveRight.type != PrimitiveKind.NULLVALUE) {
            throw new InvalidArgumentErr("IS/IS NOT", "right operand must be a null value");
        }

        Object leftValue = left.eval(relation,row);
        boolean isNull = (leftValue == null);

        return (this == Is) ? isNull : !isNull;
    }

    private Object evalStandardComparison(Relation relation,Individual row, Expression left, Expression right)
            throws EvalErr {
        Object leftValue = left.eval(relation,row);
        Object rightValue = right.eval(relation,row);

        if (leftValue == null || rightValue == null) {
            return false;
        }

        return switch (this) {
            case Eq -> areEqual(leftValue, rightValue);
            case Neq -> !areEqual(leftValue, rightValue);
            case Lt, Lte, Gt, Gte -> compareNumbers(leftValue, rightValue);
            default -> throw new InvalidArgumentErr(this.toString(), "comparison not implemented");
        };
    }
    
    private static boolean areEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null || right == null)
            return false;
        if (left.getClass() == right.getClass()) {
            return left.equals(right);
        }
        System.out.println("left : " + left + "/ right" + right);
        try {
            double leftDouble = toDouble(left);
            double rightDouble = toDouble(right);
            return leftDouble == rightDouble;
        } catch (EvalErr e) {
            return left.toString().equals(right.toString());
        }
    }

    private boolean compareNumbers(Object left, Object right) throws EvalErr {
        double leftDouble = toDouble(left);
        double rightDouble = toDouble(right);

        return switch (this) {
            case Lt -> leftDouble < rightDouble;
            case Lte -> leftDouble <= rightDouble;
            case Gt -> leftDouble > rightDouble;
            case Gte -> leftDouble >= rightDouble;
            default -> false;
        };
    }

    private static double toDouble(Object value) throws EvalErr {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        boolean boolValue = Expression.ObjectIntoBoolean(value);
        return Expression.booleanIntoDouble(boolValue);
    }

}