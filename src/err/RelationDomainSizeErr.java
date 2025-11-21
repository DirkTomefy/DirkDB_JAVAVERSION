package err;
import base.Relation;
public class RelationDomainSizeErr extends RelationalErr {
    public RelationDomainSizeErr(Relation rel1,Relation rel2){
       super(""+rel1.getName()+"("+rel1.getDomaines().size()+")"+" AND "+rel2.getName()+"("+rel2.getDomaines().size()+")");
    }
    
}
