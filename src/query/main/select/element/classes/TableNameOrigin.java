package query.main.select.element.classes;

import query.main.select.element.interfaces.TableOriginWithAlias;

public class TableNameOrigin extends TableOriginWithAlias {

    String name;

    public TableNameOrigin(String id, String alias, String name) {
        super(id, alias);
        this.name = name;
    }
}
