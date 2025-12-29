package sqlTsinjo.query.main.select;

import java.io.IOException;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;
import sqlTsinjo.query.main.select.element.enums.BasicRowOp;

public class SelectBinOpExpr extends SelectExpr {
    SelectExpr left;
    BasicRowOp op;
    SelectExpr right;

    public SelectBinOpExpr(SelectExpr left, BasicRowOp op, SelectExpr right) {
        this.left = left;
        this.op = op;
        this.right = right;
    }

    @Override
    public Relation eval(AppContext context) throws ParseNomException, EvalErr, IOException {
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

    @Override
    public Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException, EvalErr, IOException {
        return this.eval(context.getAppcontext());
    }
}
