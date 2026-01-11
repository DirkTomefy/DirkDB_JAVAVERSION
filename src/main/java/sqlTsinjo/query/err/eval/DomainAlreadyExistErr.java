package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DomainAlreadyExistErr extends EvalErr {
    public DomainAlreadyExistErr(String domainName) {
        super("Ny efitra : "+ domainName +" dia efa misys");
    }
}
