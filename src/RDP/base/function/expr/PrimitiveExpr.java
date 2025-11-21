package RDP.base.function.expr;

import java.util.Vector;

import RDP.err.EvalErr;
import RDP.err.eval.AmbigousNameErr;
import RDP.err.eval.FieldNotFoundErr;
import RDP.err.eval.InvalidArgumentErr;
import RDP.err.eval.NullValueErr;
import RDP.err.eval.TypeMismatchErr;
import base.Domain;
import base.Individual;
import base.Relation;

public class PrimitiveExpr implements Expression {
    public PrimitiveKind type;
    private final Object value;

    public Object getValue() {
        return value;
    }

    public PrimitiveExpr(PrimitiveKind type, Object value) {
        this.type = type;
        this.value = value;
    }

    public static PrimitiveExpr number(Double valueDouble) {
        return new PrimitiveExpr(PrimitiveKind.NUMBER, valueDouble);
    }

    public static PrimitiveExpr id(String valueString) {
        return new PrimitiveExpr(PrimitiveKind.ID, valueString);
    }

    public static PrimitiveExpr string(String valueString) {
        return new PrimitiveExpr(PrimitiveKind.STRING, valueString);
    }

    public static PrimitiveExpr nullvalue(){
        return new PrimitiveExpr(PrimitiveKind.NULLVALUE, null);
    }
    @Override
    public String toString() {
        return type.toString() + "(" + value + ")";
    }


    @Override
    public Object eval(Relation r,Individual row) throws EvalErr {
        return switch (type) {
            case ID -> evalId(r,row);
            case NULLVALUE -> evalNullValue();
            case NUMBER -> evalNumber();
            case STRING -> evalString();
            default -> evalDefault();
        };
    }

    private Object evalId(Relation relation,Individual row) throws EvalErr {
        Vector<String> fieldName=relation.getFieldName();

        Vector<Domain> domains=relation.getDomaines();

        handleErrForEvalId0(fieldName);

        String idFieldName = (String) value;

        int index = fieldName.indexOf(idFieldName);

        handleErrForEvalId1(idFieldName, fieldName, index);
        Object idValue = row.get(index);
        

        return idValue;
    }
    private void handleErrForEvalId0(Vector<String> fieldName) throws EvalErr {
         if (fieldName == null || fieldName.isEmpty()) {
            throw new InvalidArgumentErr("ID", "field names cannot be null or empty");
        }

        if (!(value instanceof String)) {
            throw new TypeMismatchErr("String", value);
        }
    }
    private void handleErrForEvalId1( String idFieldName,Vector<String> fieldName ,int index) throws EvalErr  {
        if(fieldName.lastIndexOf(idFieldName)!=fieldName.indexOf(idFieldName)) throw new AmbigousNameErr(idFieldName);
        
        if (index == -1) {
            throw new FieldNotFoundErr(idFieldName, fieldName);
        }
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