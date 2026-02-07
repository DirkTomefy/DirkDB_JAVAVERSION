package sqlTsinjo.query.main.sqlobject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.domains.VARCHAR;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.storage.TombstoneManager;

public enum ObjectSQLEnum {
    TABLE,
    DATABASE,
    DOMAIN,
    VIEW
    ;

    public Relation show(AppContext ctx) throws NoDatabaseSelect, IOException {
        switch (this) {
            case DATABASE:
                return listDatabases(ctx);
            case TABLE:
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                
                return Relation.makeListRelation(Path.of(ctx.getDataDirectory(),ctx.getDatabaseName(),"tables"));
            
            case DOMAIN :
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                return Relation.makeListRelation(Path.of(ctx.getDataDirectory(),ctx.getDatabaseName(),"domains"));
            
            case VIEW :
                if(ctx.getDatabaseName()==null)
                   throw new NoDatabaseSelect();
                return Relation.makeListRelation(Path.of(ctx.getDataDirectory(),ctx.getDatabaseName(),"views"));
            
            default:
                throw new IllegalArgumentException("database ou table ");

        }

    }

    private Relation listDatabases(AppContext ctx) throws IOException {
        Path root = Path.of(ctx.getDataDirectory());
        if (!Files.exists(root)) {
            return Relation.makeListRelation(root);
        }

        List<String> visible = new ArrayList<>();
        try (var stream = Files.list(root)) {
            stream.filter(Files::isDirectory)
                    .forEach(p -> {
                        if (!TombstoneManager.isDatabaseDeleted(p.toFile(), ctx.getTombstoneConfig())) {
                            visible.add(p.getFileName().toString());
                        }
                    });
        }

        String name = "fanasehoana";
        Vector<Domain> domains = new Vector<>();
        domains.add(new VARCHAR().intoDomain());

        Vector<Vector<Object>> individus = new Vector<>();
        for (String db : visible) {
            Vector<Object> ligne = new Vector<>();
            ligne.add(db);
            individus.add(ligne);
        }

        Vector<QualifiedIdentifier> qualifiedIdentifiers = new Vector<>();
        qualifiedIdentifiers.add(new QualifiedIdentifier(name, "fanasehoana"));

        Relation r = new Relation();
        r.setName(name);
        r.setDomaines(domains);
        r.setIndividus(individus);
        r.setFieldName(qualifiedIdentifiers);
        return r;
    }
}
