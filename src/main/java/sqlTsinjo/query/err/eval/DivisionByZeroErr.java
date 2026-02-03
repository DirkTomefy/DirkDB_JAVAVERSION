package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DivisionByZeroErr extends EvalErr {
    public DivisionByZeroErr() {
        super("division by zero");
    }
    
    public DivisionByZeroErr(Object numerator) {
        super("division by zero numerator = "+numerator + " / 0");
    }
}