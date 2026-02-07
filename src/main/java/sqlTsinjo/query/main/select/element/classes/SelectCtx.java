package sqlTsinjo.query.main.select.element.classes;

import java.util.LinkedHashMap;

import sqlTsinjo.cli.AppContext;
public class SelectCtx {

    /// * key : ALIAS , Value : TableName
    LinkedHashMap<String, String> aliasmap;
    AppContext appcontext;

    public SelectCtx(LinkedHashMap<String, String> aliasmap, AppContext appcontext) {
        this.aliasmap = aliasmap;
        this.appcontext = appcontext;
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
