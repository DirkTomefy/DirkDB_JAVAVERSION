package sqlTsinjo.query.base.classes.expr;

import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class FunctionExpr implements Expression {
    private final String name;
    private final Vector<Expression> args;

    public FunctionExpr(String name, Vector<Expression> args) {
        this.name = name;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public Vector<Expression> getArgs() {
        return args;
    }

    @Override
    public Object eval(Relation relation, Vector<Object> row, SelectCtx ctx) throws EvalErr {
        throw new EvalErr("Function '" + name + "' ne peut pas être évaluée ligne-par-ligne");
    }

    @Override
    public String toString() {
        return "FunctionExpr(" + name + ", " + args + ")";
    }
}
