package sqlTsinjo.base;

import java.util.HashMap;
import java.util.Vector;

import sqlTsinjo.base.err.DomainOutOfBonds;
import sqlTsinjo.base.err.DomainSupportErr;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.base.err.RelationDomainSizeErr;
import sqlTsinjo.base.util.DeleteHelper;
import sqlTsinjo.base.util.InsertHelper;
import sqlTsinjo.base.util.ProjectionHelper;
import sqlTsinjo.base.util.RelationDisplayer;
import sqlTsinjo.base.util.UpdateHelper;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.insert.element.abstracts.InsertRqstValues;
import sqlTsinjo.query.main.select.element.abstracts.SelectFields;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class Relation {

    String name;

    Vector<QualifiedIdentifier> fieldName;

    Vector<Domain> domaines = new Vector<>();
    Vector<Vector<Object>> individus = new Vector<>();

    public Vector<QualifiedIdentifier> getFieldName() {
        return fieldName;
    }

    public void setFieldName(Vector<QualifiedIdentifier> fieldName) {
        this.fieldName = fieldName;
    }

    public static boolean indEquals(Vector<Object> ind1, Vector<Object> ind2) {
        if (ind1 == null || ind2 == null)
            return false;
        if (ind1.size() != ind2.size())
            return false;

        for (int i = 0; i < ind1.size(); i++) {
            Object v1 = ind1.get(i);
            Object v2 = ind2.get(i);
            if (v1 == null && v2 == null)
                continue;
            if (v1 == null || v2 == null)
                return false;
            if(v1 instanceof char[] value)
                v1=new String(value);
            if(v2 instanceof char[] value)
                v2=new String(value);

            if (!v1.equals(v2))
                return false;
        }

        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Vector<Domain> getDomaines() {
        return domaines;
    }

    public void setDomaines(Vector<Domain> domaines) {
        this.domaines = domaines;
    }

    public Vector<Vector<Object>> getIndividus() {
        return individus;
    }

    public void setIndividus(Vector<Vector<Object>> individus) {
        this.individus = individus;
    }

    public Relation() {
        this.domaines = new Vector<>();
        this.fieldName = new Vector<>();
        this.individus = new Vector<>();
    }
    public Relation(String name, Vector<String> fieldRealName, Vector<Domain> domaines) {
        this.name = name;
        this.fieldName=QualifiedIdentifier.into(fieldRealName);
        this.domaines = domaines;
        this.individus = new Vector<>();
    }

    public Relation(String name, Vector<QualifiedIdentifier> fieldName, Vector<Domain> domaines,
            Vector<Vector<Object>> individus) {
        this.name = name;
        this.fieldName = fieldName;
        this.domaines = domaines;
        this.individus = individus;
    }

    
    public static Relation makeDualRelation() {
        String name = "dual";
        Vector<QualifiedIdentifier> id = new Vector<>();
        id.add(new QualifiedIdentifier(null, name));
        Vector<Domain> domains = new Vector<>();
        domains.add(Domain.makeUniversalDomain());

        Vector<Vector<Object>> persons=new Vector<>();
        Vector<Object> row=new Vector<>();
        persons.add(row);
        return new Relation(name, id, domains, persons );
    }

    public boolean isValidDomain(Relation rel2) {
        if (this.domaines.size() == rel2.domaines.size())
            return true;

        return false;
    }

    public void supportsWithErr(Vector<Object> ind) throws DomainOutOfBonds, DomainSupportErr {
        if (ind.size() != this.domaines.size())
            throw new DomainOutOfBonds(ind, this);
        for (int i = 0; i < ind.size(); i++) {
            if (!domaines.get(i).isSupportable(ind.get(i))) {
                throw new DomainSupportErr(ind, this.domaines.get(i), i);
            }

        }
    }

    public boolean contains(Vector<Object> ind) {
        for (Vector<Object> i : this.individus) {
            if (indEquals(ind, i)) {
                return true;
            }
        }
        return false;
    }

    public void appendIfNotExist(Vector<Object> ind) {
        if (!this.contains(ind))
            this.individus.add(ind);
    }

    public boolean support(Vector<Object> ind) {
        try {
            supportsWithErr(ind);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void insertNewInd(Vector<Object> ind) throws DomainOutOfBonds, DomainSupportErr {
        if (ind == null)
            throw new DomainSupportErr("N'insérer jamais un individu null (c'est stupide)");
        this.supportsWithErr(ind);
        this.individus.add(ind);
    }
    public void insert(Vector<String> field, InsertRqstValues values) throws DomainOutOfBonds, DomainSupportErr, EvalErr{
        InsertHelper helper = new InsertHelper(this);
        helper.insert(field, values);
    }

    public void update(HashMap<String,Expression> values,Expression condition) throws DomainOutOfBonds, DomainSupportErr, ParseNomException, EvalErr{
        UpdateHelper helper =new UpdateHelper(this);
        helper.update( values, condition);
    }
    public void delete(Expression condition) throws ParseNomException, EvalErr{
        DeleteHelper helper =new DeleteHelper(this);
        helper.delete(condition);
    }
    public static Relation union(Relation rel1, Relation rel2) throws RelationDomainSizeErr {
        String nvNom = rel1.name + "_Union_" + rel2.name;
        Vector<Domain> newDomaines = new Vector<>();
        Vector<Vector<Object>> newIndividus = new Vector<>();
        Vector<QualifiedIdentifier> fieldName = rel1.fieldName;

        if (rel1.isValidDomain(rel2)) {
            newDomaines = Domain.createNewDomain(rel1.getDomaines(), rel2.getDomaines());
            Relation result = new Relation(nvNom, fieldName, newDomaines, newIndividus);
            for (Vector<Object> i1 : rel1.individus) {
                result.appendIfNotExist(i1);
            }
            for (Vector<Object> i2 : rel2.individus) {
                result.appendIfNotExist(i2);
            }
            return result;
        } else {
            throw new RelationDomainSizeErr(rel1, rel2);
        }

    }

    public static Relation intersection(Relation rel1, Relation rel2) throws RelationDomainSizeErr {
        String nvNom = rel1.getName() + "_inter_" + rel2.getName();
        Vector<Vector<Object>> newIndividus = new Vector<>();
        if (rel1.isValidDomain(rel2)) {
            Relation result = new Relation(nvNom,rel1.fieldName , rel1.getDomaines(), newIndividus);
            for (Vector<Object> i1 : rel1.individus) {
                if (rel2.contains(i1)) {
                    result.appendIfNotExist(i1);
                }
            }
            return result;
        } else {
            throw new RelationDomainSizeErr(rel1, rel2);
        }
    }

    public static Relation difference(Relation rel1, Relation rel2) throws RelationDomainSizeErr {
        String nvNom = rel1.getName() + "_diff_" + rel2.getName();
        Vector<Domain> newDomaines = rel1.domaines;
        Vector<Vector<Object>> newIndividus = new Vector<>();
        Vector<QualifiedIdentifier> fieldName = rel1.fieldName;
        if (rel1.isValidDomain(rel2)) {
            for (Vector<Object> i1 : rel1.individus) {
                if (rel2.contains(i1)) {
                    continue;
                } else {
                    newIndividus.add(i1);
                }
            }
            return new Relation(nvNom, fieldName, newDomaines, newIndividus);
        } else {
            throw new RelationDomainSizeErr(rel1, rel2);
        }
    }

    public static Relation produitCartesien(Relation rel1, Relation rel2) {
        String nv_nom = rel1.getName() + "_produit_" + rel2.getName();
        Vector<Domain> newDomaines = new Vector<>();
        Vector<Vector<Object>> newIndividus = new Vector<>();
        Vector<QualifiedIdentifier> fieldName = new Vector<>();

        newDomaines.addAll(rel1.domaines);
        newDomaines.addAll(rel2.domaines);
        fieldName.addAll(rel1.fieldName);
        fieldName.addAll(rel2.fieldName);

        for (Vector<Object> i1 : rel1.individus) {
            for (Vector<Object> i2 : rel2.individus) {

                Vector<Object> values = new Vector<>();
                values.addAll(i1);
                values.addAll(i2);
                Vector<Object> newInd = new Vector<Object>(values);
                newIndividus.add(newInd);
            }
        }
        return new Relation(nv_nom, fieldName, newDomaines, newIndividus);
    }

    public Relation projection(SelectFields fields, SelectCtx ctx) throws EvalErr {
        ProjectionHelper helper = new ProjectionHelper(this, fields, ctx);
        return helper.executeProjection();
    }

    public Relation selection(Expression condition, SelectCtx ctx) throws ParseNomException, EvalErr {
        String newName = this.name + "_selection";
        Vector<Vector<Object>> selectedInd = new Vector<>();
        Relation result = new Relation(newName, this.fieldName, this.domaines, selectedInd);
        for (Vector<Object> individual : individus) {
            Object resultEval = condition.eval(this, individual, ctx);
            boolean conditionMet = Expression.ObjectIntoBoolean(resultEval);

            if (conditionMet) {
                result.appendIfNotExist(individual);
            }
        }
        return result;
    }

    public Relation jointureExterneGauche(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr {
        Relation produit = produitCartesien(this, tojoin);
        Relation jointureInterne = condition != null ? produit.selection(condition, ctx) : produit;
        String newName = this.name + "_left_join_" + tojoin.name;

        Vector<Domain> newDomains = new Vector<>();
        newDomains.addAll(this.domaines);
        newDomains.addAll(tojoin.domaines);

        Vector<QualifiedIdentifier> newFieldNames = new Vector<>();
        newFieldNames.addAll(this.fieldName);
        newFieldNames.addAll(tojoin.fieldName);

        Vector<Vector<Object>> newIndividus = new Vector<>();

        Relation result = new Relation(newName, newFieldNames, newDomains, newIndividus);

        // 4. Ajouter tous les tuples issus de la jointure interne
        for (Vector<Object> ind : jointureInterne.individus) {
            result.appendIfNotExist(ind);
        }

        // 5. Pour chaque individu de la relation gauche
        for (Vector<Object> leftInd : this.individus) {

            boolean found = false;

            for (Vector<Object> joinedInd : jointureInterne.individus) {
                // comparer uniquement la partie gauche
                boolean same = true;
                for (int i = 0; i < leftInd.size(); i++) {
                    if (!Relation.indEquals(
                            new Vector<>(leftInd.subList(i, i + 1)),
                            new Vector<>(joinedInd.subList(i, i + 1)))) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    found = true;
                    break;
                }
            }

            // 6. Si aucune correspondance → ajouter avec NULL à droite
            if (!found) {
                Vector<Object> newInd = new Vector<>();
                newInd.addAll(leftInd);

                for (int i = 0; i < tojoin.fieldName.size(); i++) {
                    newInd.add(null);
                }

                result.appendIfNotExist(newInd);
            }
        }

        return result;
    }

    public Relation jointureExterneDroite(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr {
        return tojoin.jointureExterneGauche(this, condition, ctx);
    }

    public Relation jointureExternePleine(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr, RelationDomainSizeErr {

        // LEFT OUTER JOIN
        Relation leftJoin = this.jointureExterneGauche(tojoin, condition, ctx);

        // RIGHT OUTER JOIN
        Relation rightJoin = this.jointureExterneDroite(tojoin, condition, ctx);

        // UNION des deux (supprime les doublons)
        return Relation.union(leftJoin, rightJoin);
    }

    public Relation jointureNaturelle(Relation tojoin, Expression condition, SelectCtx ctx) {
        return null;
    }

    public Relation jointureInterne(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr {
        return condition != null ? produitCartesien(this, tojoin).selection(condition, ctx)
                : produitCartesien(this, tojoin);
    }

    public Relation unionWithCondition(Relation rel2, Expression condition,SelectCtx ctx) throws ParseNomException, EvalErr{ 
        return condition != null ? union(this, rel2).selection(condition, ctx)
                : union(this, rel2);
    }

    public Relation intersectionWithCondition(Relation rel2, Expression condition,SelectCtx ctx) throws ParseNomException, EvalErr{ 
        return condition != null ? intersection(this, rel2).selection(condition, ctx)
                : intersection(this, rel2);
    }
    public Relation differenceWithCondition(Relation rel2, Expression condition,SelectCtx ctx) throws ParseNomException, EvalErr{ 
        return condition != null ? difference(this, rel2).selection(condition, ctx)
                : difference(this, rel2);
    }


    @Override
    public String toString() {
        return RelationDisplayer.display(this);
    }

    public String toStringDebug() {
        return RelationDisplayer.displayDebug(this);
    }
}
