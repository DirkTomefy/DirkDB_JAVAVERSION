package err;

import base.Domain;
import base.Individual;

public class DomainSupportErr extends RelationalErr{
    public DomainSupportErr() {
        super();
    }
    public DomainSupportErr(String message) {
        super(message);
    }
    public DomainSupportErr(Throwable cause) {
        super(cause);
    }
    public DomainSupportErr(Individual ind,Domain d,int i){
        super("ind : "+ind.toString() +" can not convert into domain d "+d.toString()+" at values i="+i);
    }
}
