package RDP.err;

import RDP.token.Token;

public class ParseNomException extends Exception {
    String input;

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public ParseNomException(String input, String message) {
        super(message);
        this.input = input;
    }

    public ParseNomException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ParseNomException buildTagException(String input,String expected) {
        return new ParseNomException(input,"can't not tag : " + expected);
    }

    public static ParseNomException buildMultiSpace1Excpetion(String input) {
        return new ParseNomException(input,"Expected at least one whitespace");
    }

    public static ParseNomException buildTokenWrongPlace(Token token,String input){
        return new ParseNomException(input,"Token wrong place : "+token.toString());
    }

    public static ParseNomException buildParensMissing(Token token,String input){
        return new ParseNomException(input,"Parense missing : / Token wrong place : "+token.toString());
    }

    public static ParseNomException buildRemainingException(String remaining){
        return new ParseNomException(remaining, "Can not eval because the input contains some remaining='"+remaining+"'");
    }
}
