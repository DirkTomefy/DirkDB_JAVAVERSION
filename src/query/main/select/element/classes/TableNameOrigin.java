package query.main.select.element.classes;
import java.util.LinkedHashMap;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import query.err.eval.AmbigousAliasErr;
import query.main.select.element.abstracts.TableOriginWithAlias;
import storage.SerdeRelation;

public class TableNameOrigin extends TableOriginWithAlias {

    String name;

    public TableNameOrigin(String id, String alias, String name) {
        super(id, alias);
        this.name = name;
    }

    @Override
    public String toString() {
        return "TableNameOrigin [name=" + name + ", id=" + id + ", alias=" + alias + "]";
    }

    @Override
    public Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException, EvalErr {
        SerdeRelation serdeRelation =new SerdeRelation(context.getAppcontext(),name);
        return serdeRelation.deserializeRelation();
    }

    @Override
    public void makeAliasAsTableOrigin(LinkedHashMap<String, String> aliasMap) throws AmbigousAliasErr {
        aliasMap.put(name, id );
        if (aliasMap.containsKey(alias)) {
            throw new AmbigousAliasErr("Alias '" + alias + "' déjà utilisé");
        } else {
            aliasMap.put(alias, id);
        }
    }
}
