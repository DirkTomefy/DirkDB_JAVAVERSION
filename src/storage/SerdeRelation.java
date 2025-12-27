package storage;

import java.util.HashMap;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import cli.AppContext;
import debug.function.TestSelection;
import query.base.ParseSuccess;
import query.main.select.SelectRqst;
public class SerdeRelation {
    AppContext appContext;
    String tableName;
    public SerdeRelation(AppContext appContext, String tableName) {
        this.appContext = appContext;
        this.tableName = tableName;
    }
    public static HashMap<String,Relation> makeFaker(){
        HashMap<String,Relation> faker=new HashMap<>();
        faker.put("test.emp1", TestSelection.makeRelationOne());
        faker.put("test.emp2", TestSelection.makeRelationTwo());
        faker.put("test.code", TestSelection.makeRelationThree());
        return faker;
    } 
    public void serializeRelation(Relation rel){
        
    }
    public Relation deserializeRelation(){
        return makeFaker().get(appContext.getDatabaseName()+"."+tableName);
    }

    public static void main(String[] args) throws ParseNomException, EvalErr {
        ParseSuccess<SelectRqst> select=SelectRqst.parseSelect("Alaivo c1.Nom #ao@ code c1  #tonona:avia@ code c3");
        System.out.println(""+select);
        Relation r = select.matched().eval(new AppContext("test", null));
        System.out.println(""+r.toStringDebug());
    }
}
