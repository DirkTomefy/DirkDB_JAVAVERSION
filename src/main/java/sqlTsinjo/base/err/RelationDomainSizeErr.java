package sqlTsinjo.base.err;
import sqlTsinjo.base.Relation;
public class RelationDomainSizeErr extends EvalErr {
    public RelationDomainSizeErr(Relation rel1,Relation rel2){
       super(""+rel1.getName()+"("+rel1.getDomaines().size()+")"+" AND "+rel2.getName()+"("+rel2.getDomaines().size()+")");
    }
    
}
