package sqlTsinjo.query.main.select;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Vector;

import sqlTsinjo.base.Domain;
import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.FunctionExpr;
import sqlTsinjo.query.base.classes.expr.PrimitiveExpr;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.element.classes.FieldSelectedList;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;

import static org.junit.jupiter.api.Assertions.*;

public class GroupByTest {

    @Test
    void parse_group_by_single_column() throws Exception {
        String input = "alaivo 1 #vondrona (a)";
        ParseSuccess<SelectRqst> parsed = SelectRqst.parseSelect(input);
        assertNotNull(parsed.matched());
        assertNotNull(parsed.matched().groupBy);
        assertEquals(1, parsed.matched().groupBy.size());
        assertEquals("a", parsed.matched().groupBy.get(0).getName());
    }

    @Test
    void parse_group_by_multiple_columns() throws Exception {
        String input = "alaivo 1 #vondrona (a, t1.b)";
        ParseSuccess<SelectRqst> parsed = SelectRqst.parseSelect(input);
        assertNotNull(parsed.matched());
        assertNotNull(parsed.matched().groupBy);
        assertEquals(2, parsed.matched().groupBy.size());
        assertEquals("a", parsed.matched().groupBy.get(0).getName());
        assertNull(parsed.matched().groupBy.get(0).getOrigin());
        assertEquals("b", parsed.matched().groupBy.get(1).getName());
        assertEquals("t1", parsed.matched().groupBy.get(1).getOrigin());
    }

    @Test
    void parse_group_by_without_parentheses_fails() {
        String input = "alaivo 1 #vondrona a";
        assertThrows(ParseNomException.class, () -> SelectRqst.parseSelect(input));
    }

    @Test
    void parse_function_expr_count() throws Exception {
        String input = "alaivo count(a)";
        SelectRqst rqst = SelectRqst.parseSelect(input).matched();
        assertNotNull(rqst);
        assertTrue(rqst.fields instanceof FieldSelectedList);

        FieldSelectedList list = (FieldSelectedList) rqst.fields;
        assertEquals(1, list.size());
        assertTrue(list.get(0).getExpr() instanceof FunctionExpr);

        FunctionExpr fn = (FunctionExpr) list.get(0).getExpr();
        assertEquals("count", fn.getName().toLowerCase());
        assertEquals(1, fn.getArgs().size());
    }

    @Test
    void eval_group_by_count_sum_avg_on_relation() throws Exception {
        Vector<QualifiedIdentifier> fieldNames = new Vector<>();
        fieldNames.add(new QualifiedIdentifier(null, "a"));
        fieldNames.add(new QualifiedIdentifier(null, "b"));

        Vector<Domain> domains = new Vector<>();
        domains.add(Domain.makeUniversalDomain());
        domains.add(Domain.makeUniversalDomain());

        Vector<Vector<Object>> rows = new Vector<>();
        Vector<Object> r1 = new Vector<>();
        r1.add("x");
        r1.add(1.0);
        rows.add(r1);
        Vector<Object> r2 = new Vector<>();
        r2.add("x");
        r2.add(2.0);
        rows.add(r2);
        Vector<Object> r3 = new Vector<>();
        r3.add("y");
        r3.add(null);
        rows.add(r3);

        Relation src = new Relation("t", fieldNames, domains, rows);

        Vector<QualifiedIdentifier> groupBy = new Vector<>();
        groupBy.add(new QualifiedIdentifier(null, "a"));

        Vector<sqlTsinjo.query.base.classes.expr.Expression> countArgs = new Vector<>();
        countArgs.add(PrimitiveExpr.id(new QualifiedIdentifier(null, "b")));

        Vector<sqlTsinjo.query.base.classes.expr.Expression> sumArgs = new Vector<>();
        sumArgs.add(PrimitiveExpr.id(new QualifiedIdentifier(null, "b")));

        Vector<sqlTsinjo.query.base.classes.expr.Expression> avgArgs = new Vector<>();
        avgArgs.add(PrimitiveExpr.id(new QualifiedIdentifier(null, "b")));

        FieldSelectedList select = new FieldSelectedList();
        select.add(new sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias(
                PrimitiveExpr.id(new QualifiedIdentifier(null, "a")), null));
        select.add(new sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias(
                new FunctionExpr("count", countArgs), null));
        select.add(new sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias(
                new FunctionExpr("sum", sumArgs), null));
        select.add(new sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias(
                new FunctionExpr("avg", avgArgs), null));

        SelectCtx ctx = new SelectCtx(new LinkedHashMap<>(), new AppContext(null, "test", false));
        Relation out = src.groupBy(select, groupBy, ctx);

        assertEquals(2, out.getIndividus().size());

        // ordre non garanti => on cherche les lignes par valeur de a
        Vector<Object> rowX = null;
        Vector<Object> rowY = null;
        for (Vector<Object> rr : out.getIndividus()) {
            if ("x".equals(rr.get(0))) {
                rowX = rr;
            } else if ("y".equals(rr.get(0))) {
                rowY = rr;
            }
        }
        assertNotNull(rowX);
        assertNotNull(rowY);

        assertEquals(2.0, ((Number) rowX.get(1)).doubleValue(), 0.0000001);
        assertEquals(3.0, ((Number) rowX.get(2)).doubleValue(), 0.0000001);
        assertEquals(1.5, ((Number) rowX.get(3)).doubleValue(), 0.0000001);

        assertEquals(0.0, ((Number) rowY.get(1)).doubleValue(), 0.0000001);
        assertEquals(0.0, ((Number) rowY.get(2)).doubleValue(), 0.0000001);
        assertEquals(0.0, ((Number) rowY.get(3)).doubleValue(), 0.0000001);
    }

    @Test
    void eval_without_group_by_works_on_dual() throws Exception {
        String input = "alaivo 1";
        SelectRqst rqst = SelectRqst.parseSelect(input).matched();

        AppContext ctx = new AppContext(null, "test", false);
        Relation r = rqst.eval(ctx);

        assertNotNull(r);
        assertNotNull(r.getIndividus());
        assertEquals(1, r.getIndividus().size());
        assertEquals(1, r.getIndividus().get(0).size());
        assertEquals(1.0, ((Number) r.getIndividus().get(0).get(0)).doubleValue(), 0.0000001);
    }
}
