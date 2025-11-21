package RDP.err.parsing;

import RDP.err.ParseNomException;

public class AfterIsOrIsNotErr extends ParseNomException{
    public AfterIsOrIsNotErr(String input){
     super(input,"After 'is' or 'is not' must be 'null' ");
    }
}
