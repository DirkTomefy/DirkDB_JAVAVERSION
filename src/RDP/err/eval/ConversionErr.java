package RDP.err.eval;

import RDP.err.EvalErr;

public class ConversionErr extends EvalErr {
    public ConversionErr(String fromType, String toType, Object value) {
        super("Cannot convert " + fromType + " to " + toType + ": " + value);
    }
    
    public ConversionErr(String fromType, String toType, Object value, String reason) {
        super("Cannot convert " + fromType + " to " + toType + ": " + value + " (" + reason + ")");
    }
}