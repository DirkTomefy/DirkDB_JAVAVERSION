package sqlTsinjo.query.main.sqlobject;

import java.io.IOException;
import java.nio.file.Path;

import sqlTsinjo.base.Relation;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;

public enum ObjectSQLEnum {
    TABLE,
    DATABASE,
    DOMAIN,
    VIEW
    ;

    public Relation show(AppContext ctx) throws NoDatabaseSelect, IOException {
        switch (this) {
            case DATABASE:
                return Relation.makeListRelation(Path.of("databases/"));
            case TABLE:
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                
                return Relation.makeListRelation(Path.of("databases/",ctx.getDatabaseName(),"tables"));
            
            case DOMAIN :
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                return Relation.makeListRelation(Path.of("databases/",ctx.getDatabaseName(),"domains"));
            
            case VIEW :
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                return Relation.makeListRelation(Path.of("databases/",ctx.getDatabaseName(),"views"));
            
            default:
                throw new IllegalArgumentException("database ou table ");

        }

    }
}
