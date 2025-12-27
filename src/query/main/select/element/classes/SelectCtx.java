package query.main.select.element.classes;

import java.util.LinkedHashMap;

import query.err.eval.AmbigousAliasErr;
import query.main.select.SelectRqst;

public class SelectCtx {
    ///* key : ALIAS , Value : TableName 
    LinkedHashMap<String,String> aliasmap;

    public SelectCtx() {
        this.aliasmap=new LinkedHashMap<>();
    }

    public SelectCtx(SelectRqst rqst){
        //TODO : rempli cette fonction aprés avoir mis les jointures
    }
    public SelectCtx(LinkedHashMap<String, String> aliasmap) {
        this.aliasmap = aliasmap;
    }

    public LinkedHashMap<String, String> getAliasmap() {
        return aliasmap;
    }

    public void addAlias(String alias, String tableName) throws AmbigousAliasErr {
        // Vérifier si l'alias existe déjà
        if (aliasmap.containsKey(alias)) {
            throw new AmbigousAliasErr("Alias '" + alias + "' déjà utilisé");
        }
        
        aliasmap.put(alias, tableName);
    }

    public void setAliasmap(LinkedHashMap<String, String> aliasmap) {
        this.aliasmap = aliasmap;
    }
}
