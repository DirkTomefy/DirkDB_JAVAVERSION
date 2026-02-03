package sqlTsinjo.query.main.select.element.err;

import sqlTsinjo.base.err.ParseNomException;

public class AliasNeededException extends ParseNomException{

    public AliasNeededException(String input) {
        super(input, "Tous les tables dérivé doit avoir leur propre alias");
    }
    
}
