package base.util;

import java.util.Vector;

import base.Domain;
import base.Individual;

public class RelationManager {
      public static Vector<String> createFields(String... fields) {
        Vector<String> result = new Vector<>();
        for (String field : fields) {
            result.add(field);
        }
        return result;
    }

    public static Vector<Domain> createDomains(Domain... domains) {
        Vector<Domain> result = new Vector<>();
        for (Domain domain : domains) {
            result.add(domain);
        }
        return result;
    }

    public static Vector<Individual> createIndividuals(Object[]... individuals) {
        Vector<Individual> result = new Vector<>();
        for (Object[] individual : individuals) {
            result.add(new Individual(individual));
        }
        return result;
    }
}
