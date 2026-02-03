package sqlTsinjo.base.err;

import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.util.RelationDisplayer;
 

public class DomainSupportErr extends EvalErr{
    public DomainSupportErr(String message) {
        super(message);
    }
    public DomainSupportErr(Throwable cause) {
        super(cause);
    }
    public DomainSupportErr( Vector<Object> ind,Domain d,int i){
        super("can not convert into domain d : "+d.toString()+" at values ind="+i+" { "+ RelationDisplayer.formatObjectIntoDebugVersion(ind.get(i))+" } ");
    }
   
}
