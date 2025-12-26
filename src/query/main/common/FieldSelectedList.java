package query.main.common;
import java.util.Vector;

import query.base.classes.expr.Expression;
import query.main.select.element.classes.FieldElementWithAlias;
import query.main.select.element.interfaces.SelectFields;

public class FieldSelectedList extends Vector<FieldElementWithAlias> implements SelectFields {

    public FieldSelectedList(FieldElementWithAlias first_element) {
       super();
       this.add(first_element);
    }
    public FieldSelectedList(Expression first_expression){
       super();
       this.add(new FieldElementWithAlias(first_expression, null));
    }
    
}
