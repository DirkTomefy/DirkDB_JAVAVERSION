package sqlTsinjo.base.err;

 
import java.util.Vector;

import sqlTsinjo.base.Relation;

public class DomainOutOfBonds extends EvalErr {

    public DomainOutOfBonds(String message) {
        super(message);
    }

    public DomainOutOfBonds(Throwable cause) {
        super(cause);
    }

    public DomainOutOfBonds( Vector<Object> ind, Relation rel) {
        super("Cannot add  Individual of fields_size="  + ind.size()
                + " in relation(" + rel.getName() + ") with domains_size " + rel.getDomaines().size());
    }
}
