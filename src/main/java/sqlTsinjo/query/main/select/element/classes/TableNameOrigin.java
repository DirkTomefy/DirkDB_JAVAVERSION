package sqlTsinjo.query.main.select.element.classes;
import java.io.IOException;
import java.util.LinkedHashMap;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.err.eval.AmbigousAliasErr;
import sqlTsinjo.query.main.select.element.abstracts.TableOriginWithAlias;
import sqlTsinjo.storage.SerdeRelation;

public class TableNameOrigin extends TableOriginWithAlias {

    String name;

    public TableNameOrigin() {
    }

    public TableNameOrigin(String id, String alias, String name) {
        super(id, alias);
        this.name = name;
    }

    @Override
    public String toString() {
        return "TableNameOrigin [name=" + name + ", id=" + id + ", alias=" + alias + "]";
    }

    @Override
    public Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException,EvalErr, IOException {
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
