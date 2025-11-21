package debug.function;

import java.util.Vector;

import RDP.err.EvalErr;
import RDP.err.ParseNomException;
import base.Domain;
import base.Individual;
import base.Relation;
import base.domains.NUMBER;
import base.domains.VARCHAR;

public class TestSelection {
    public static void main(String[] args) throws ParseNomException, EvalErr {
        Vector<String> fieldName=new Vector<>();
        fieldName.add("id");
        fieldName.add("nom");
        fieldName.add("ville");

        Vector<Domain> domains=new Vector<>();
        domains.add(new NUMBER().intoDomain());
        domains.add(new VARCHAR(30).intoDomain());
        domains.add(new VARCHAR(30).intoDomain());

        Vector<Individual> inVector=new Vector<>();
        inVector.add(new Individual(new Object[]{1,"bogosy","paris"}));
        inVector.add(new Individual(new Object[]{2,"paris","paris"}));
        inVector.add(new Individual(new Object[]{3,"mpisandoka","marseille"}));

        Relation r=new Relation("relation test", fieldName, domains, inVector);

      //  System.out.println(""+r);
        String condition="nom=ville";
        System.out.println("Condition :"+condition);
        System.out.println(""+r.selection(condition));
    }
}
