package sqlTsinjo.query.err.parsing.token;

import sqlTsinjo.base.err.ParseNomException;

public class TokenNotFound extends ParseNomException {
    public TokenNotFound(String input){
     super(input,"Token available not found near : '"+input+"'");
    }
}
