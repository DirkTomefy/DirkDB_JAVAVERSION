package sqlTsinjo.query.err.parsing;

import sqlTsinjo.base.err.ParseNomException;

public class CommandAvailableNotFound extends ParseNomException{

    public CommandAvailableNotFound(String input) {
        super(input, "Commande valide non trouvé");
       
    }
    
}
