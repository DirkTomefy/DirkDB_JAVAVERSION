package base;

import java.util.Vector;

import RDP.base.ParseSuccess;
import RDP.base.function.expr.Expression;
import RDP.err.EvalErr;
import RDP.err.ParseNomException;
import RDP.err.eval.FieldNotFoundErr;
import base.util.ProjectionHelper;
import base.util.RelationDisplayer;
import err.DomainOutOfBonds;
import err.DomainSupportErr;
import err.RelationDomainSizeErr;

public class Relation {

    String name;

    Vector<String> fieldName;
    Vector<Domain> domaines = new Vector<>();
    Vector<Vector<Object>> individus = new Vector<>();

    public Vector<String> getFieldName() {
        return fieldName;
    }

    public void setFieldName(Vector<String> fieldName) {
        this.fieldName = fieldName;
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

    public Relation(String name, Vector<String> fieldName, Vector<Domain> domaines, Vector<Vector<Object>> individus) {
        this.name = name;
        this.fieldName = fieldName;
        this.domaines = domaines;
        this.individus = individus;
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
        boolean value = false;
        for (Vector<Object> i : this.individus) {
            if (ind.equals(i)) {
                return true;
            }
        }
        return value;
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

    // Insérer un nouveau individu : Object[]
    public void insertNewInd(Vector<Object> ind) throws DomainOutOfBonds, DomainSupportErr {
        this.supportsWithErr(ind);
        this.individus.add(ind);
    }

    
    public static Relation union(Relation rel1, Relation rel2) throws RelationDomainSizeErr {
        String nvNom = rel1.name + "_Union_" + rel2.name;
        Vector<Domain> newDomaines = new Vector<>();
        Vector<Vector<Object>> newIndividus = new Vector<>();
        Vector<String> fieldName = rel1.fieldName;

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
        Vector<Domain> newDomaines = rel1.domaines;
        Vector<Vector<Object>> newIndividus = new Vector<>();
        Vector<String> fieldName = rel1.fieldName;
        if (rel1.isValidDomain(rel2)) {
            Relation result = new Relation(nvNom, fieldName, newDomaines, newIndividus);
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
        Vector<String> fieldName = rel1.fieldName;
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
        Vector<String> fieldName = new Vector<>();

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

    public Relation projection(String[] fields) throws FieldNotFoundErr {
        ProjectionHelper helper = new ProjectionHelper();
        return helper.executeProjection(this, fields);
    }

    public Relation selection(String condition) throws ParseNomException, EvalErr {
        ParseSuccess<Expression> exprSuccess = Expression.parseExpression.apply(condition);
        if (!exprSuccess.remaining().trim().isEmpty())
            throw ParseNomException.buildRemainingException(exprSuccess.remaining());

        Expression expr = exprSuccess.matched();
        String newName = this.name + "_selection";
        Vector<Vector<Object>> selectedInd = new Vector<>();
        Relation result = new Relation(newName, this.fieldName, this.domaines, selectedInd);
        for (Vector<Object> individual : individus) {
            Object resultEval = expr.eval(this, individual);
            boolean conditionMet = Expression.ObjectIntoBoolean(resultEval);

            if (conditionMet) {
                result.appendIfNotExist(individual);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return RelationDisplayer.display(this);
    }

    public String toStringDebug() {
        return RelationDisplayer.displayDebug(this);
    }
}
