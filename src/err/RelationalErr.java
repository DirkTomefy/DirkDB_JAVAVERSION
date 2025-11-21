package err;

public class RelationalErr extends Exception{
    public RelationalErr() {
        super();
    }
    public RelationalErr(String message) {
        super(message);
    }
    public RelationalErr(Throwable cause) {
        super(cause);
    }
    public RelationalErr(String message, Throwable cause) {
        super(message, cause);
    }

}
