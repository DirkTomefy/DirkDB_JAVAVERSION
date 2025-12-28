package sqlTsinjo.base.err;

public class EvalErr extends RelationalErr{
    public EvalErr(String message){
        super(message);
    }
     public EvalErr(Throwable t){
        super(t);
    }
}
