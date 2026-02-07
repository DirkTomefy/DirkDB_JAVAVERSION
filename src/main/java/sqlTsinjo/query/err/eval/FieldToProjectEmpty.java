package sqlTsinjo.query.err.eval;

import sqlTsinjo.base.err.EvalErr;

public class FieldToProjectEmpty extends EvalErr{
    public FieldToProjectEmpty(){
        super("Champ à projeter vide : ");
    }
}
