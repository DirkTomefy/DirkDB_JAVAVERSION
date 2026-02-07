package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class DomainNotFound extends EvalErr {
    public DomainNotFound(String database , String domainName) {
        super("La table "+database+"."+domainName+" n'as pas été trouvé");
    }
}
