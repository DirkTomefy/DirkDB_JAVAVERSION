package query.main.select.element.classes;

import java.util.LinkedHashMap;

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

    public void setAliasmap(LinkedHashMap<String, String> aliasmap) {
        this.aliasmap = aliasmap;
    }
}
