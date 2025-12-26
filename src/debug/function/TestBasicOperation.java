package debug.function;

import java.util.Arrays;
import java.util.Vector;

// import base.Domain;
// import base.Relation;
// import base.domains.VARCHAR;
import base.err.RelationDomainSizeErr;

public class TestBasicOperation {
    public static Vector<Vector<Object>> makeIndividusForRelOne() {
        Vector<Vector<Object>> rows = new Vector<>();

        // Individu 1
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("1","a"))));
        // Individu 2
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("2","b"))));

        // Individu 3 avec un NULL
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("3","c"))));

        return rows;
    }

    public static Vector<Vector<Object>> makeIndividusForRelTwo() {
        Vector<Vector<Object>> rows = new Vector<>();

        // Individu 1
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("2","b"))));
        // Individu 2
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("5","a"))));

        // Individu 3 avec un NULL
        rows.add(new Vector<Object>(new Vector<>(Arrays.asList("4","n"))));

        return rows;
    }

    public static void main(String[] args) throws RelationDomainSizeErr {
        // Vector<String> fieldName = new Vector<>();
        // fieldName.add("id");
        // fieldName.add("name");
        // Vector<Domain> d = new Vector<>();
        // d.add(new VARCHAR(1).intoDomain());

        // Relation rel1 = new Relation("rel1", fieldName, d, makeIndividusForRelOne());
        // Relation rel2 = new Relation("rel2", fieldName, d, makeIndividusForRelTwo());

        // System.out.println(""+Relation.intersection(rel2, rel1)+"\n");   

        // System.out.println(""+Relation.difference(rel1, rel2)+"\n");  
        
        // System.out.println(""+Relation.difference(rel2, rel1)+"\n");   

        // System.out.println(""+Relation.union(rel1, rel2)+"\n");   
        
        // System.out.println(""+Relation.produitCartesien(rel1, rel2)+"\n");        
        
    }
}
