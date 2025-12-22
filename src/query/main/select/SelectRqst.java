package query.main.select;

import java.util.Vector;

import query.base.classes.expr.Expression;
import query.main.FieldName;
import query.main.select.element.interfaces.TableOrigin;

public class SelectRqst implements TableOrigin {
    Vector<FieldName> fields;
    TableOrigin from;
    Expression where;
}
