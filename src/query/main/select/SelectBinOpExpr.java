package query.main.select;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import cli.AppContext;
import query.main.select.element.enums.BasicRowOp;

public class SelectBinOpExpr implements SelectExpr {
    SelectExpr left;
    BasicRowOp op;
    SelectExpr right;

    public SelectBinOpExpr(SelectExpr left, BasicRowOp op, SelectExpr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Relation eval(AppContext context) throws ParseNomException, EvalErr {
        Relation l = left.eval(context);
        Relation r = right.eval(context);
        Relation result = l;
        switch (op) {
            case DIFFERENCE:
                result = Relation.difference(l, r);
                break;
            case INTERSECTION:
                result = Relation.intersection(l, r);
                break;
            case UNION:
                result = Relation.union(l, r);
                break;
            default:
                break;
          
        }
      return result;
    }

    @Override
    public String toString() {
        return "SelectBinOpExpr [left=" + left + ", op=" + op + ", right=" + right + "]";
    }
}
