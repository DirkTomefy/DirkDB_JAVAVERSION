package err;

 
import java.util.Vector;

import base.Relation;

public class DomainOutOfBonds extends RelationalErr {
    public DomainOutOfBonds() {

    }

    public DomainOutOfBonds(String message) {
        super(message);
    }

    public DomainOutOfBonds(Throwable cause) {
        super(cause);
    }

    public DomainOutOfBonds( Vector<Object> ind, Relation rel) {
        super("Cannot add  Vector<Object> of fields_size=" + ind.size()
                + " in relation(" + rel.getName() + ") with domains_size " + rel.getDomaines().size());
    }
}
