package sqlTsinjo.query.main.select.element.classes;

import sqlTsinjo.query.base.classes.expr.Expression;

public class FieldElementWithAlias {
    Expression expr;
    String alias;
    public FieldElementWithAlias() {
    }
    public FieldElementWithAlias(Expression expr, String alias) {
        this.expr = expr;
        this.alias = alias;
    }
    @Override
    public String toString() {
        return "FieldElementWithAlias { expr (" + expr + ") as " + alias + " }";
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public Expression getExpr() {
        return expr;
    }
    public String getAlias() {
        return alias;
    }
}
