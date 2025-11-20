package RDP.err.eval;

import RDP.err.EvalErr;

public class InvalidArgumentErr extends EvalErr {
    public InvalidArgumentErr(String functionName, String reason) {
        super("Invalid argument for " + functionName + ": " + reason);
    }
    
    public InvalidArgumentErr(String functionName, int expected, int actual) {
        super("Invalid argument for " + functionName + ": expected " + expected + " arguments, got " + actual);
    }
}