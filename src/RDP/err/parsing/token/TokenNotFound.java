package RDP.err.parsing.token;

import RDP.err.ParseNomException;

public class TokenNotFound extends ParseNomException {
    public TokenNotFound(String input){
     super(input,"Token available not found near : '"+input+"'");
    }
}
