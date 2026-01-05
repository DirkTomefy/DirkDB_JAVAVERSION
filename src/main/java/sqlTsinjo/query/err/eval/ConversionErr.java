package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class ConversionErr extends EvalErr {
    public ConversionErr(String fromType, String toType, Object value) {
        super("Tsy afaka @domain " + fromType + " ho " + toType + ": " + value);
    }
    
    public ConversionErr(String fromType, String toType, Object value, String reason) {
        super("Tsy afaka @domain " + fromType + " ho " + toType + ": " + value + " (" + reason + ")");
    }
}