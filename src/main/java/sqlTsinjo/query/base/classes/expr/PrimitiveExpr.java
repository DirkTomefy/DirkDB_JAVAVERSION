package sqlTsinjo.query.base.classes.expr;

import java.util.Vector;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.err.eval.NullValueErr;
import sqlTsinjo.query.err.eval.TypeMismatchErr;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class PrimitiveExpr implements Expression {
    public PrimitiveKind type;
    private Object value;

    public PrimitiveExpr() {
        // Default constructor for Jackson
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public PrimitiveKind getType() {
        return type;
    }

    public void setType(PrimitiveKind type) {
        this.type = type;
    }

    public PrimitiveExpr(PrimitiveKind type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static PrimitiveExpr number(Double valueDouble) {
        return new PrimitiveExpr(PrimitiveKind.NUMBER, valueDouble);
    }

    public static PrimitiveExpr id(QualifiedIdentifier valueString) {
        return new PrimitiveExpr(PrimitiveKind.ID, valueString);
    }

    public static PrimitiveExpr string(String valueString) {
        return new PrimitiveExpr(PrimitiveKind.STRING, valueString);
    }

    public static PrimitiveExpr nullvalue() {
        return new PrimitiveExpr(PrimitiveKind.NULLVALUE, null);
    }

    @Override
    public String toString() {
        return type.toString() + "(" + value + ")";
    }

    @Override
    public Object eval(Relation r, Vector<Object> row,SelectCtx ctx) throws EvalErr {
        return switch (type) {
            case ID -> evalId(r, row,ctx);
            case NULLVALUE -> evalNullValue();
            case NUMBER -> evalNumber();
            case STRING -> evalString();
            default -> evalDefault();
        };
    }

    private Object evalId(Relation relation, Vector<Object> row,SelectCtx ctx) throws EvalErr {
        QualifiedIdentifier idFieldName = (QualifiedIdentifier) value;
        int index = idFieldName.getIndex(relation.getFieldName(),ctx);
        Object idValue = idFieldName.getValueFromARow(relation.getFieldName(), row, index,ctx);
        String maybeReturn = handleVarcharAndChar(idValue);
        if (maybeReturn != null) {
            return maybeReturn;
        } else {
            return idValue;
        }
    }

    private String handleVarcharAndChar( Object idvalue) {
        if(idvalue instanceof char[] c) return new String(c);
        if(idvalue instanceof String s) return s;
        return null;
    }

    private Object evalNumber() throws EvalErr {
        if (value instanceof Number) {
            return value;
        }
        throw new TypeMismatchErr("Number", value);
    }

    private Object evalString() throws EvalErr {
        if (value instanceof String) {
            return value;
        }
        throw new TypeMismatchErr("String", value);
    }

    private Object evalNullValue() {
        return null;
    }

    private Object evalDefault() throws EvalErr {
        if (value != null) {
            return value;
        }
        throw new NullValueErr("default primitive expression");
    }
}