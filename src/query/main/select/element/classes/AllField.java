package query.main.select.element.classes;

import query.main.select.element.interfaces.SelectFields;

public class AllField implements SelectFields {

    @Override
    public String toString() {
        return "AllField (*) ";
    }
}
