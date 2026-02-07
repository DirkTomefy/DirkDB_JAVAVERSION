package sqlTsinjo.query.main.select.element.classes;

import java.util.Vector;

import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.select.element.abstracts.SelectFields;

public class FieldSelectedList extends Vector<FieldElementWithAlias> implements SelectFields {

   public FieldSelectedList(FieldElementWithAlias first_element) {
      super();
      this.add(first_element);
   }

   public FieldSelectedList() {

   }

   public FieldSelectedList(Expression first_expression) {
      super();
      this.add(new FieldElementWithAlias(first_expression, null));
   }

}
