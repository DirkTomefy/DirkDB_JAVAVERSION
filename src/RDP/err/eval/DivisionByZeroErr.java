package RDP.err.eval;

import base.err.EvalErr;

public class DivisionByZeroErr extends EvalErr {
    public DivisionByZeroErr() {
        super("division by zero");
    }
    
    public DivisionByZeroErr(Object numerator) {
        super("division by zero numerator = "+numerator + " / 0");
    }
}