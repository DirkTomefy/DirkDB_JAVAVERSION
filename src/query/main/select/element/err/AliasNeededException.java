package query.main.select.element.err;

import base.err.ParseNomException;

public class AliasNeededException extends ParseNomException{

    public AliasNeededException(String input) {
        super(input, "Tous les tables dérivé doit avoir leur propre alias");
    }
    
}
