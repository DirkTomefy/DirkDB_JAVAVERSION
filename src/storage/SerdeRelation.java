package storage;

import base.Relation;
import cli.AppContext;
public class SerdeRelation {
    AppContext appContext;
    String tableName;
    public SerdeRelation(AppContext appContext, String tableName) {
        this.appContext = appContext;
        this.tableName = tableName;
    }
    public void serializeRelation(Relation rel){

    }
    public Relation deserializeRelation(){
        return null;
    }
}
