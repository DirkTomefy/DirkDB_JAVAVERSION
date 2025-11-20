package RDP.err.eval;

import RDP.err.EvalErr;

public class TypeMismatchErr extends EvalErr {
    public TypeMismatchErr(String expectedType, String actualType) {
        super("Type mismatch: expected " + expectedType + " but got " + actualType);
    }
    
    public TypeMismatchErr(String expectedType, Object actualValue) {
        super("Type mismatch: expected " + expectedType + " but got " + 
              (actualValue != null ? actualValue.getClass().getSimpleName() : "null"));
    }
}
