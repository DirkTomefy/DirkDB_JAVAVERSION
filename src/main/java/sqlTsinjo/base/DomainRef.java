package sqlTsinjo.base;

import java.io.IOException;
import java.util.Vector;

import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.err.eval.DomainNotFound;
import sqlTsinjo.query.err.eval.NoDatabaseSelect;
import sqlTsinjo.storage.SerdeDomain;

public class DomainRef extends DomainAtom {
    private final String domainName;

    public DomainRef(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public static Domain resolveNonPrimitiveDomain(
            Domain domain,
            SerdeDomain serde) throws  IOException, NoDatabaseSelect, DomainNotFound {

        Vector<DomainAtom> resolvedAtoms = new Vector<>();

        for (DomainAtom atom : domain.getSupports()) {

            // Cas primitif → conserver
            if (!(atom instanceof DomainRef)) {
                resolvedAtoms.add(atom);
                continue;
            }

            // Cas non primitif → résoudre
            DomainRef ref = (DomainRef) atom;

            serde.setDomainName(ref.getDomainName());
            Domain resolved = serde.deserializeDomain();
            resolvedAtoms.addAll(resolved.getSupports());
        }

        return new Domain(resolvedAtoms);
    }

    public static void resolveAllNonPrimitiveDomains(
            Vector<Domain> domains,
            AppContext ctx) throws  NoDatabaseSelect, IOException, DomainNotFound {


        SerdeDomain serde = new SerdeDomain(ctx, null);

        for (int i = 0; i < domains.size(); i++) {
            Domain resolved = resolveNonPrimitiveDomain(domains.get(i), serde);
            domains.set(i, resolved);
        }
    }

    @Override
    public boolean isSupportable(Object value) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isSupportable'");
    }
}
