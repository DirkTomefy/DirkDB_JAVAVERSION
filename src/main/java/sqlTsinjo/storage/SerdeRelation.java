package sqlTsinjo.storage;

import java.util.HashMap;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.debug.function.TestSelection;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.main.select.SelectExpr;
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
        ParseSuccess<SelectExpr> select=SelectExpr.parseExpr("Alaivo * #ao@ code c1 \n #atifitra@ (Alaivo * #ao@ code) ");
        System.out.println(""+select);
        Relation r = select.matched().eval(new AppContext("test", null));
        System.out.println(""+r.toStringDebug());
    }
}
