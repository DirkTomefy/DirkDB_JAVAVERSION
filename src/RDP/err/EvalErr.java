package RDP.err;

import err.RelationalErr;

public class EvalErr extends RelationalErr{
    public EvalErr(String message){
        super(message);
    }
     public EvalErr(Throwable t){
        super(t);
    }
}
