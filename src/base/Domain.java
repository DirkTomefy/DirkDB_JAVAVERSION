package base;

import java.util.Vector;

import base.domains.DATE;
import base.domains.NUMBER;
import base.domains.VARCHAR;
import base.domains.abstracts.DBString;

public class Domain {
    Vector<DomainAtom> supports;

    public Domain() {
        this.supports = new Vector<>();
    }

    public Domain(Vector<DomainAtom> supports) {
        this.supports = supports;
    }

    public void add(DomainAtom values) {
        this.supports.add(values);
    }

    public static Domain createNewDomain(Domain d1, Domain d2) {
        if (d1 == null && d2 == null)
            return new Domain();
        if (d1 == null)
            return new Domain(d2.supports);
        if (d2 == null)
            return new Domain(d1.supports);

        Vector<DomainAtom> newSupports = new Vector<>();
        newSupports.addAll(d1.supports);
        newSupports.addAll(d2.supports);
        return new Domain(newSupports);
    }

    public static Vector<Domain> createNewDomain(Vector<Domain> d1, Vector<Domain> d2) {
        Vector<Domain> result = new Vector<>();

        if (d1 == null && d2 == null)
            return result;
        if (d1 == null)
            return new Vector<>(d2);
        if (d2 == null)
            return new Vector<>(d1);

        int maxSize = Math.max(d1.size(), d2.size());

        for (int i = 0; i < maxSize; i++) {
            Domain domain1 = i < d1.size() ? d1.get(i) : null;
            Domain domain2 = i < d2.size() ? d2.get(i) : null;
            Domain combinedDomain = createNewDomain(domain1, domain2);
            result.add(combinedDomain);
        }

        return result;
    }

    public boolean isSupportable(Object a) {
        for (DomainAtom domainAtom : supports) {
            if ((a == null && domainAtom.getCanBenull()) || domainAtom.isSupportable(a))
                return  true ;
        }
        return false;
    }

    @Override
    public String toString() {
        if (supports == null || supports.isEmpty()) {
            return "Domain{EMPTY}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Domain{");

        int count = 0;
        for (DomainAtom atom : supports) {
            if (count > 0)
                sb.append(", ");

            if (atom != null) {
                sb.append("" + atom.toString());
            } else {
                sb.append("NULL");
            }
            count++;
        }
        sb.append("}");

        return sb.toString();
    }

    public Vector<DomainAtom> getSupports() {
        return supports;
    }

    public DBString<?> getStringVersion() {
        for (DomainAtom domainAtom : supports) {
            if (domainAtom instanceof DBString e)
                return e;
        }
        return null;
    }
    public static Domain makeUniversalDomain(){
        Domain d=new Domain();
        d.add(new VARCHAR(null));
        d.add(new DATE());
        d.add(new NUMBER());
        return d;
    }
}
