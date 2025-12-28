package sqlTsinjo.query.main.select.element.err;

import sqlTsinjo.base.err.ParseNomException;

public class AliasWithSourceCodeException extends ParseNomException{
    public AliasWithSourceCodeException(String input){
        super(input , "un alias ne doit pas contenir un source code '.' ");
    }
}
