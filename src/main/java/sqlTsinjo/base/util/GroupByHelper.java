package sqlTsinjo.base.util;

import java.util.HashMap;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.classes.expr.FunctionExpr;
import sqlTsinjo.query.base.classes.expr.PrimitiveExpr;
import sqlTsinjo.query.err.eval.AmbigousNameErr;
import sqlTsinjo.query.err.eval.FieldNotFoundErr;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.element.abstracts.SelectFields;
import sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias;
import sqlTsinjo.query.main.select.element.classes.FieldSelectedList;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

public class GroupByHelper {
    private final Relation src;
    private final SelectFields fields;
    private final Vector<QualifiedIdentifier> groupBy;
    private final SelectCtx ctx;

    public GroupByHelper(Relation src, SelectFields fields, Vector<QualifiedIdentifier> groupBy, SelectCtx ctx) {
        this.src = src;
        this.fields = fields;
        this.groupBy = groupBy;
        this.ctx = ctx;
    }

    public Relation execute() throws EvalErr {
        FieldSelectedList list = requireFieldSelectedListForGroupBy(fields);
        validateGroupByKeys(groupBy);

        final int selectSize = list.size();
        boolean[] isAgg = new boolean[selectSize];
        QualifiedIdentifier[] groupSelectQid = new QualifiedIdentifier[selectSize];
        AggState[] aggStateBySelect = new AggState[selectSize];

        HashMap<Integer, Integer> groupByIndexByColumnIndex = buildGroupByIndexByColumnIndex(groupBy);
        analyzeGroupBySelectList(list, selectSize, isAgg, groupSelectQid, aggStateBySelect, groupByIndexByColumnIndex);

        HashMap<GroupKey, GroupAcc> groups = buildGroups(list, selectSize, isAgg, groupSelectQid, aggStateBySelect);
        return buildGroupByResult(list, selectSize, isAgg, groupSelectQid, groups);
    }

    private FieldSelectedList requireFieldSelectedListForGroupBy(SelectFields fields) throws EvalErr {
        if (!(fields instanceof FieldSelectedList list)) {
            throw new EvalErr("GROUP BY n'est supporté que pour une liste explicite de champs");
        }
        return list;
    }

    private void validateGroupByKeys(Vector<QualifiedIdentifier> groupBy) throws EvalErr {
        if (groupBy == null || groupBy.isEmpty()) {
            throw new EvalErr("GROUP BY demande au moins une clé");
        }
    }

    private HashMap<Integer, Integer> buildGroupByIndexByColumnIndex(Vector<QualifiedIdentifier> groupBy) throws EvalErr {
        HashMap<Integer, Integer> groupByIndexByColumnIndex = new HashMap<>();
        for (int i = 0; i < groupBy.size(); i++) {
            QualifiedIdentifier qid = groupBy.get(i);
            try {
                int colIndex = qid.getIndex(src.getFieldName(), ctx);
                groupByIndexByColumnIndex.put(colIndex, i);
            } catch (AmbigousNameErr | FieldNotFoundErr e) {
                throw new EvalErr(e);
            }
        }
        return groupByIndexByColumnIndex;
    }

    private void analyzeGroupBySelectList(FieldSelectedList list, int selectSize, boolean[] isAgg,
            QualifiedIdentifier[] groupSelectQid, AggState[] aggStateBySelect,
            HashMap<Integer, Integer> groupByIndexByColumnIndex) throws EvalErr {
        for (int i = 0; i < selectSize; i++) {
            FieldElementWithAlias fe = list.get(i);
            Expression expr = fe.getExpr();

            if (expr instanceof FunctionExpr fn) {
                String name = fn.getName() == null ? "" : fn.getName().trim().toLowerCase();
                validateGroupByAggFunction(fn, name);
                int colIndex = resolveAggColumnIndex(fn);
                isAgg[i] = true;
                aggStateBySelect[i] = new AggState(name, colIndex);
            } else if (expr instanceof PrimitiveExpr p && p.getValue() instanceof QualifiedIdentifier qid) {
                int colIndex;
                try {
                    colIndex = qid.getIndex(src.getFieldName(), ctx);
                } catch (AmbigousNameErr | FieldNotFoundErr e) {
                    throw new EvalErr(e);
                }
                if (!groupByIndexByColumnIndex.containsKey(colIndex)) {
                    throw new EvalErr("Champ non agrégé doit être dans #vondrona: " + qid);
                }
                groupSelectQid[i] = qid;
            } else {
                throw new EvalErr("Expression non supportée avec GROUP BY: " + expr);
            }
        }
    }

    private void validateGroupByAggFunction(FunctionExpr fn, String normalizedName) throws EvalErr {
        if (!(normalizedName.equals("count") || normalizedName.equals("sum") || normalizedName.equals("avg"))) {
            throw new EvalErr("Fonction non supportée dans GROUP BY: " + fn.getName());
        }
        if (fn.getArgs() == null || fn.getArgs().size() != 1) {
            throw new EvalErr("" + fn.getName() + " attend exactement 1 argument");
        }
        Expression arg = fn.getArgs().get(0);
        if (!(arg instanceof PrimitiveExpr p) || !(p.getValue() instanceof QualifiedIdentifier)) {
            throw new EvalErr("" + fn.getName() + " attend un identifiant de colonne (ex: " + fn.getName() + "(a))");
        }
    }

    private int resolveAggColumnIndex(FunctionExpr fn) throws EvalErr {
        Expression arg = fn.getArgs().get(0);
        PrimitiveExpr p = (PrimitiveExpr) arg;
        QualifiedIdentifier qidArg = (QualifiedIdentifier) p.getValue();
        try {
            return qidArg.getIndex(src.getFieldName(), ctx);
        } catch (AmbigousNameErr | FieldNotFoundErr e) {
            throw new EvalErr(e);
        }
    }

    private HashMap<GroupKey, GroupAcc> buildGroups(FieldSelectedList list, int selectSize, boolean[] isAgg,
            QualifiedIdentifier[] groupSelectQid, AggState[] aggStateBySelect) throws EvalErr {
        HashMap<GroupKey, GroupAcc> groups = new HashMap<>();
        for (Vector<Object> row : src.getIndividus()) {
            GroupKey gk = buildGroupKey(row);
            GroupAcc acc = groups.get(gk);
            if (acc == null) {
                acc = createGroupAcc(selectSize, isAgg, aggStateBySelect);
                groups.put(gk, acc);
            }
            updateGroupAcc(row, selectSize, isAgg, groupSelectQid, acc);
        }
        return groups;
    }

    private GroupKey buildGroupKey(Vector<Object> row) throws EvalErr {
        Vector<Object> keyValues = new Vector<>();
        keyValues.setSize(groupBy.size());
        for (int i = 0; i < groupBy.size(); i++) {
            QualifiedIdentifier qid = groupBy.get(i);
            Object v;
            try {
                v = qid.getValueFromARow(src.getFieldName(), row, ctx);
            } catch (AmbigousNameErr | FieldNotFoundErr e) {
                throw new EvalErr(e);
            }
            keyValues.set(i, v);
        }
        return new GroupKey(keyValues);
    }

    private GroupAcc createGroupAcc(int selectSize, boolean[] isAgg, AggState[] aggStateBySelect) {
        AggState[] freshAggs = new AggState[selectSize];
        for (int i = 0; i < selectSize; i++) {
            if (isAgg[i]) {
                AggState template = aggStateBySelect[i];
                freshAggs[i] = new AggState(template.getFn(), template.getColumnIndex());
            }
        }
        return new GroupAcc(selectSize, freshAggs);
    }

    private void updateGroupAcc(Vector<Object> row, int selectSize, boolean[] isAgg, QualifiedIdentifier[] groupSelectQid,
            GroupAcc acc) throws EvalErr {
        for (int i = 0; i < selectSize; i++) {
            if (!isAgg[i]) {
                if (acc.getSelectGroupValue(i) == null) {
                    QualifiedIdentifier qid = groupSelectQid[i];
                    Object v;
                    try {
                        v = qid.getValueFromARow(src.getFieldName(), row, ctx);
                    } catch (AmbigousNameErr | FieldNotFoundErr e) {
                        throw new EvalErr(e);
                    }
                    acc.setSelectGroupValue(i, v);
                }
            } else {
                updateAggFromRow(row, acc.getAgg(i));
            }
        }
    }

    private void updateAggFromRow(Vector<Object> row, AggState st) throws EvalErr {
        if (st == null) {
            return;
        }

        Object value = row.get(st.getColumnIndex());
        if (value == null) {
            return;
        }

        switch (st.getFn()) {
            case "count" -> st.incrementCount();
            case "sum" -> {
                if (!(value instanceof Number n)) {
                    throw new EvalErr("SUM attend une valeur numérique, trouvé: " + value.getClass().getName());
                }
                st.addToSum(n.doubleValue());
            }
            case "avg" -> {
                if (!(value instanceof Number n)) {
                    throw new EvalErr("AVG attend une valeur numérique, trouvé: " + value.getClass().getName());
                }
                st.addToSum(n.doubleValue());
                st.incrementCount();
            }
            default -> throw new EvalErr("Fonction non supportée dans GROUP BY: " + st.getFn());
        }
    }

    private Relation buildGroupByResult(FieldSelectedList list, int selectSize, boolean[] isAgg,
            QualifiedIdentifier[] groupSelectQid, HashMap<GroupKey, GroupAcc> groups) throws EvalErr {
        Vector<QualifiedIdentifier> resultFieldNames = buildGroupByResultFieldNames(list, selectSize, isAgg,
                groupSelectQid);

        Vector<Domain> resultDomains = new Vector<>();
        for (int i = 0; i < selectSize; i++) {
            resultDomains.add(Domain.makeUniversalDomain());
        }

        Vector<Vector<Object>> resultIndividus = new Vector<>();
        Relation result = new Relation(src.getName() + "_groupBy", resultFieldNames, resultDomains, resultIndividus);
        for (GroupAcc acc : groups.values()) {
            resultIndividus.add(buildGroupByOutputRow(selectSize, isAgg, acc));
        }
        return result;
    }

    private Vector<QualifiedIdentifier> buildGroupByResultFieldNames(FieldSelectedList list, int selectSize,
            boolean[] isAgg, QualifiedIdentifier[] groupSelectQid) {
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
        return resultFieldNames;
    }

    private Vector<Object> buildGroupByOutputRow(int selectSize, boolean[] isAgg, GroupAcc acc) throws EvalErr {
        Vector<Object> out = new Vector<>();
        for (int i = 0; i < selectSize; i++) {
            if (!isAgg[i]) {
                out.add(acc.getSelectGroupValue(i));
            } else {
                out.add(computeAggOutputValue(acc.getAgg(i)));
            }
        }
        return out;
    }

    private Object computeAggOutputValue(AggState st) throws EvalErr {
        if (st == null) {
            return null;
        }

        switch (st.getFn()) {
            case "count" -> {
                return (double) st.getCount();
            }
            case "sum" -> {
                return st.getSum();
            }
            case "avg" -> {
                return st.getCount() == 0 ? 0.0 : (st.getSum() / st.getCount());
            }
            default -> throw new EvalErr("Fonction non supportée dans GROUP BY: " + st.getFn());
        }
    }
}
