package query.main.select.element.classes;

import java.util.LinkedHashMap;

import cli.AppContext;
import query.err.eval.AmbigousAliasErr;

public class SelectCtx {

    /// * key : ALIAS , Value : TableName
    LinkedHashMap<String, String> aliasmap;
    AppContext appcontext;

    public SelectCtx(LinkedHashMap<String, String> aliasmap, AppContext appcontext) {
        this.aliasmap = aliasmap;
        this.appcontext = appcontext;
    }

    public void addAlias(String alias, String tableName) throws AmbigousAliasErr {
        // Vérifier si l'alias existe déjà
        if (aliasmap.containsKey(alias)) {
            throw new AmbigousAliasErr("Alias '" + alias + "' déjà utilisé");
        }

        aliasmap.put(alias, tableName);
    }

    public LinkedHashMap<String, String> getAliasmap() {
        return aliasmap;
    }

    public AppContext getAppcontext() {
        return appcontext;
    }

    public void setAliasmap(LinkedHashMap<String, String> aliasmap) {
        this.aliasmap = aliasmap;
    }
}
