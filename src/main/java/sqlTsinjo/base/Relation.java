package sqlTsinjo.base;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import sqlTsinjo.base.domains.VARCHAR;
import sqlTsinjo.base.err.DomainOutOfBonds;
import sqlTsinjo.base.err.DomainSupportErr;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.base.err.RelationDomainSizeErr;
import sqlTsinjo.base.err.RelationalErr;
import sqlTsinjo.base.util.DeleteHelper;
import sqlTsinjo.base.util.InsertHelper;
import sqlTsinjo.base.util.NaturalJoinHelper;
import sqlTsinjo.base.util.ProjectionHelper;
import sqlTsinjo.base.util.RelationDisplayer;
import sqlTsinjo.base.util.UpdateHelper;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.classes.expr.FunctionExpr;
import sqlTsinjo.query.base.classes.expr.PrimitiveExpr;
import sqlTsinjo.query.err.eval.AmbigousNameErr;
import sqlTsinjo.query.err.eval.FieldNotFoundErr;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.insert.element.abstracts.InsertRqstValues;
import sqlTsinjo.query.main.select.element.abstracts.SelectFields;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;
import sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias;
import sqlTsinjo.query.main.select.element.classes.FieldSelectedList;

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

    public static Relation makeListRelation(Path dossier) throws IOException {
        String name = "fanasehoana";

        Vector<Domain> domains = new Vector<>();
        domains.add(new VARCHAR().intoDomain());

        Vector<Vector<Object>> individus = new Vector<>();

        Vector<QualifiedIdentifier> qualifiedIdentifiers = new Vector<>();
        qualifiedIdentifiers.add(new QualifiedIdentifier(name, "fanasehoana"));

       
            if(dossier.toFile().exists()){
                Files.list(dossier)
                    .map(path -> path.getFileName().toString())
                    .map(nom -> nom.split("\\.")[0]) // enlève .json, .txt, etc.
                    .forEach(nomSansExt -> {
                        Vector<Object> ligne = new Vector<>();
                        ligne.add(nomSansExt);
                        individus.add(ligne);
                    });
            }
        Relation r = new Relation();
        r.name = name;
        r.domaines = domains;
        r.individus = individus;
        r.fieldName = qualifiedIdentifiers;

        return r;
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
            if (v1 instanceof char[] value)
                v1 = new String(value);
            if (v2 instanceof char[] value)
                v2 = new String(value);

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
        this.fieldName = QualifiedIdentifier.into(fieldRealName);
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

        Vector<Vector<Object>> persons = new Vector<>();
        Vector<Object> row = new Vector<>();
        persons.add(row);
        Relation retour = new Relation(name, id, domains, persons);

        return retour;
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

    public void insert(Vector<String> field, InsertRqstValues values,AppContext context)
            throws ParseNomException, RelationalErr, IOException {
        InsertHelper helper = new InsertHelper(this);
        helper.insert(field, values,context);
    }

    public void update(HashMap<String, Expression> values, Expression condition)
            throws DomainOutOfBonds, DomainSupportErr, ParseNomException, EvalErr {
        UpdateHelper helper = new UpdateHelper(this);
        helper.update(values, condition);
    }

    public void delete(Expression condition) throws ParseNomException, EvalErr {
        DeleteHelper helper = new DeleteHelper(this);
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
            Relation result = new Relation(nvNom, rel1.fieldName, rel1.getDomaines(), newIndividus);
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

    private static final class GroupKey {
        private final Vector<Object> values;

        private GroupKey(Vector<Object> values) {
            this.values = values;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof GroupKey other))
                return false;
            if (this.values.size() != other.values.size())
                return false;
            for (int i = 0; i < this.values.size(); i++) {
                if (!Relation.objectsEqual(this.values.get(i), other.values.get(i))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = 1;
            for (Object v : values) {
                int h;
                if (v == null) {
                    h = 0;
                } else if (v instanceof char[] c) {
                    h = Arrays.hashCode(c);
                } else {
                    h = v.hashCode();
                }
                result = 31 * result + h;
            }
            return result;
        }
    }

    private static final class AggState {
        final String fn;
        final int columnIndex;
        long count;
        double sum;

        AggState(String fn, int columnIndex) {
            this.fn = fn;
            this.columnIndex = columnIndex;
        }
    }

    private static final class GroupAcc {
        final Object[] selectGroupValues;
        final AggState[] aggs;

        GroupAcc(int selectSize, AggState[] aggs) {
            this.selectGroupValues = new Object[selectSize];
            this.aggs = aggs;
        }
    }

    public Relation groupBy(SelectFields fields, Vector<QualifiedIdentifier> groupBy, SelectCtx ctx) throws EvalErr {
        if (!(fields instanceof FieldSelectedList list)) {
            throw new EvalErr("GROUP BY n'est supporté que pour une liste explicite de champs");
        }
        if (groupBy == null || groupBy.isEmpty()) {
            throw new EvalErr("GROUP BY demande au moins une clé");
        }

        final int selectSize = list.size();

        boolean[] isAgg = new boolean[selectSize];
        QualifiedIdentifier[] groupSelectQid = new QualifiedIdentifier[selectSize];
        AggState[] aggStateBySelect = new AggState[selectSize];

        HashMap<Integer, Integer> groupByIndexByColumnIndex = new HashMap<>();
        for (int i = 0; i < groupBy.size(); i++) {
            QualifiedIdentifier qid = groupBy.get(i);
            try {
                int colIndex = qid.getIndex(this.fieldName, ctx);
                groupByIndexByColumnIndex.put(colIndex, i);
            } catch (AmbigousNameErr | FieldNotFoundErr e) {
                throw new EvalErr(e);
            }
        }

        for (int i = 0; i < selectSize; i++) {
            FieldElementWithAlias fe = list.get(i);
            Expression expr = fe.getExpr();

            if (expr instanceof FunctionExpr fn) {
                String name = fn.getName() == null ? "" : fn.getName().trim().toLowerCase();
                if (!(name.equals("count") || name.equals("sum") || name.equals("avg"))) {
                    throw new EvalErr("Fonction non supportée dans GROUP BY: " + fn.getName());
                }
                if (fn.getArgs() == null || fn.getArgs().size() != 1) {
                    throw new EvalErr("" + fn.getName() + " attend exactement 1 argument");
                }
                Expression arg = fn.getArgs().get(0);
                if (!(arg instanceof PrimitiveExpr p) || !(p.getValue() instanceof QualifiedIdentifier qidArg)) {
                    throw new EvalErr("" + fn.getName() + " attend un identifiant de colonne (ex: " + fn.getName() + "(a))");
                }
                try {
                    int colIndex = qidArg.getIndex(this.fieldName, ctx);
                    isAgg[i] = true;
                    aggStateBySelect[i] = new AggState(name, colIndex);
                } catch (AmbigousNameErr | FieldNotFoundErr e) {
                    throw new EvalErr(e);
                }
            } else if (expr instanceof PrimitiveExpr p && p.getValue() instanceof QualifiedIdentifier qid) {
                try {
                    int colIndex = qid.getIndex(this.fieldName, ctx);
                    if (!groupByIndexByColumnIndex.containsKey(colIndex)) {
                        throw new EvalErr("Champ non agrégé doit être dans #vondrona: " + qid);
                    }
                    groupSelectQid[i] = qid;
                } catch (AmbigousNameErr | FieldNotFoundErr e) {
                    throw new EvalErr(e);
                }
            } else {
                throw new EvalErr("Expression non supportée avec GROUP BY: " + expr);
            }
        }

        HashMap<GroupKey, GroupAcc> groups = new HashMap<>();

        for (Vector<Object> row : this.individus) {
            Vector<Object> keyValues = new Vector<>();
            keyValues.setSize(groupBy.size());

            for (int i = 0; i < groupBy.size(); i++) {
                QualifiedIdentifier qid = groupBy.get(i);
                Object v;
                try {
                    v = qid.getValueFromARow(this.fieldName, row, ctx);
                } catch (AmbigousNameErr | FieldNotFoundErr e) {
                    throw new EvalErr(e);
                }
                keyValues.set(i, v);
            }

            GroupKey gk = new GroupKey(keyValues);
            GroupAcc acc = groups.get(gk);
            if (acc == null) {
                AggState[] freshAggs = new AggState[selectSize];
                for (int i = 0; i < selectSize; i++) {
                    if (isAgg[i]) {
                        AggState template = aggStateBySelect[i];
                        freshAggs[i] = new AggState(template.fn, template.columnIndex);
                    }
                }
                acc = new GroupAcc(selectSize, freshAggs);
                groups.put(gk, acc);
            }

            for (int i = 0; i < selectSize; i++) {
                if (!isAgg[i]) {
                    if (acc.selectGroupValues[i] == null) {
                        QualifiedIdentifier qid = groupSelectQid[i];
                        Object v;
                        try {
                            v = qid.getValueFromARow(this.fieldName, row, ctx);
                        } catch (AmbigousNameErr | FieldNotFoundErr e) {
                            throw new EvalErr(e);
                        }
                        acc.selectGroupValues[i] = v;
                    }
                } else {
                    AggState st = acc.aggs[i];
                    Object value = row.get(st.columnIndex);

                    if (value == null) {
                        continue;
                    }

                    switch (st.fn) {
                        case "count" -> st.count++;
                        case "sum" -> {
                            if (!(value instanceof Number n)) {
                                throw new EvalErr("SUM attend une valeur numérique, trouvé: " + value.getClass().getName());
                            }
                            st.sum += n.doubleValue();
                        }
                        case "avg" -> {
                            if (!(value instanceof Number n)) {
                                throw new EvalErr("AVG attend une valeur numérique, trouvé: " + value.getClass().getName());
                            }
                            st.sum += n.doubleValue();
                            st.count++;
                        }
                        default -> throw new EvalErr("Fonction non supportée dans GROUP BY: " + st.fn);
                    }
                }
            }
        }

        Vector<QualifiedIdentifier> resultFieldNames = new Vector<>();
        for (int i = 0; i < selectSize; i++) {
            FieldElementWithAlias fe = list.get(i);
            Expression expr = fe.getExpr();
            if (fe.getAlias() != null) {
                resultFieldNames.add(new QualifiedIdentifier(null, fe.getAlias()));
            } else if (!isAgg[i] && groupSelectQid[i] != null) {
                resultFieldNames.add(groupSelectQid[i]);
            } else {
                resultFieldNames.add(new QualifiedIdentifier(null, expr.toString()));
            }
        }

        Vector<Domain> resultDomains = new Vector<>();
        for (int i = 0; i < selectSize; i++) {
            resultDomains.add(Domain.makeUniversalDomain());
        }

        Vector<Vector<Object>> resultIndividus = new Vector<>();
        Relation result = new Relation(this.name + "_groupBy", resultFieldNames, resultDomains, resultIndividus);

        for (GroupAcc acc : groups.values()) {
            Vector<Object> out = new Vector<>();
            for (int i = 0; i < selectSize; i++) {
                if (!isAgg[i]) {
                    out.add(acc.selectGroupValues[i]);
                } else {
                    AggState st = acc.aggs[i];
                    switch (st.fn) {
                        case "count" -> out.add((double) st.count);
                        case "sum" -> out.add(st.sum);
                        case "avg" -> out.add(st.count == 0 ? 0.0 : (st.sum / st.count));
                        default -> throw new EvalErr("Fonction non supportée dans GROUP BY: " + st.fn);
                    }
                }
            }
            resultIndividus.add(out);
        }

        return result;
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

        String newName = this.name + "_right_join_" + tojoin.name;

        // 1. Produit cartésien
        Relation produit = produitCartesien(this, tojoin);

        // 2. Sélection avec condition (jointure interne)
        Relation jointureInterne = condition != null ? produit.selection(condition, ctx) : produit;

        // 3. Préparer les métadonnées du résultat
        Vector<Domain> newDomains = new Vector<>();
        Vector<QualifiedIdentifier> newFieldNames = new Vector<>();

        // Dans RIGHT JOIN, on garde l'ordre: gauche puis droite
        newDomains.addAll(this.domaines);
        newDomains.addAll(tojoin.domaines);
        newFieldNames.addAll(this.fieldName);
        newFieldNames.addAll(tojoin.fieldName);

        Vector<Vector<Object>> newIndividus = new Vector<>();
        Relation result = new Relation(newName, newFieldNames, newDomains, newIndividus);

        // 4. Ajouter tous les tuples de la jointure interne
        for (Vector<Object> ind : jointureInterne.individus) {
            result.appendIfNotExist(ind);
        }

        // 5. Pour chaque individu de la relation DROITE (tojoin)
        for (Vector<Object> rightInd : tojoin.individus) {

            boolean found = false;

            // Vérifier si cet individu droit a un correspondant à gauche
            for (Vector<Object> joinedInd : jointureInterne.individus) {
                // Comparer uniquement la partie droite
                boolean same = true;
                int leftSize = this.fieldName.size();

                for (int i = 0; i < rightInd.size(); i++) {
                    if (!Relation.indEquals(
                            new Vector<>(rightInd.subList(i, i + 1)),
                            new Vector<>(joinedInd.subList(leftSize + i, leftSize + i + 1)))) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    found = true;
                    break;
                }
            }

            // 6. Si aucune correspondance → ajouter avec NULL à gauche
            if (!found) {
                Vector<Object> newInd = new Vector<>();

                // Ajouter NULL pour toutes les colonnes de gauche
                for (int i = 0; i < this.fieldName.size(); i++) {
                    newInd.add(null);
                }

                // Ajouter les valeurs de la relation droite
                newInd.addAll(rightInd);

                result.appendIfNotExist(newInd);
            }
        }

        return result;
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

    public Relation jointureNaturelle(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr {
        return condition != null ? jointureNaturelle(tojoin).selection(condition, ctx) : jointureNaturelle(tojoin);
    }

    public Relation jointureInterne(Relation tojoin, Expression condition, SelectCtx ctx)
            throws ParseNomException, EvalErr {
        return condition != null ? produitCartesien(this, tojoin).selection(condition, ctx)
                : produitCartesien(this, tojoin);
    }

    public Relation jointureNaturelle(Relation tojoin) {
        NaturalJoinHelper helper = new NaturalJoinHelper(this, tojoin);
        return helper.execute();
    }

    // Méthode utilitaire pour comparer des objets (gère les nulls)
    public static boolean objectsEqual(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;

        // Gestion spéciale pour les char[]
        if (o1 instanceof char[] && o2 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        }
        if (o1 instanceof char[] && o2 instanceof String) {
            return Arrays.equals((char[]) o1, ((String) o2).toCharArray());
        }
        if (o1 instanceof String && o2 instanceof char[]) {
            return Arrays.equals(((String) o1).toCharArray(), (char[]) o2);
        }

        return o1.equals(o2);
    }

    @Override
    public String toString() {
        return RelationDisplayer.display(this);
    }

    public String toStringDebug() {
        return RelationDisplayer.displayDebug(this);
    }

    public long count(QualifiedIdentifier field, SelectCtx ctx) throws EvalErr {
        try {
            int index = field.getIndex(this.fieldName, ctx);
            long count = 0;
            for (Vector<Object> row : this.individus) {
                Object value = row.get(index);
                if (value != null) {
                    count++;
                }
            }
            return count;
        } catch (AmbigousNameErr | FieldNotFoundErr e) {
            throw new EvalErr(e);
        }
    }

    public double sum(QualifiedIdentifier field, SelectCtx ctx) throws EvalErr {
        try {
            int index = field.getIndex(this.fieldName, ctx);
            double sum = 0.0;
            boolean seenAny = false;
            for (Vector<Object> row : this.individus) {
                Object value = row.get(index);
                if (value == null) {
                    continue;
                }
                if (!(value instanceof Number number)) {
                    throw new EvalErr("SUM attend une valeur numérique, trouvé: " + value.getClass().getName());
                }
                sum += number.doubleValue();
                seenAny = true;
            }
            return seenAny ? sum : 0.0;
        } catch (AmbigousNameErr | FieldNotFoundErr e) {
            throw new EvalErr(e);
        }
    }

    public double avg(QualifiedIdentifier field, SelectCtx ctx) throws EvalErr {
        try {
            int index = field.getIndex(this.fieldName, ctx);
            double sum = 0.0;
            long count = 0;
            for (Vector<Object> row : this.individus) {
                Object value = row.get(index);
                if (value == null) {
                    continue;
                }
                if (!(value instanceof Number number)) {
                    throw new EvalErr("AVG attend une valeur numérique, trouvé: " + value.getClass().getName());
                }
                sum += number.doubleValue();
                count++;
            }
            return count == 0 ? 0.0 : (sum / count);
        } catch (AmbigousNameErr | FieldNotFoundErr e) {
            throw new EvalErr(e);
        }
    }
}
