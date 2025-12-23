package query.main.common;

import query.main.select.element.interfaces.TableOrigin;

public record QualifiedIdentifier(String origin,String name) implements TableOrigin {
    
}
