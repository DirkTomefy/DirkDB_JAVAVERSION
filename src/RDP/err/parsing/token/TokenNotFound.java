package RDP.err.parsing.token;

import base.err.ParseNomException;

public class TokenNotFound extends ParseNomException {
    public TokenNotFound(String input){
     super(input,"Token available not found near : '"+input+"'");
    }
}
