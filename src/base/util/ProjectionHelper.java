package base.util;

import java.util.Vector;

import base.Domain;
import base.Relation;
import base.err.EvalErr;
import query.base.classes.expr.PrimitiveExpr;
import query.err.eval.AmbigousAliasErr;
import query.err.eval.AmbigousNameErr;
import query.err.eval.FieldNotFoundErr;
import query.main.common.QualifiedIdentifier;
import query.main.select.element.abstracts.SelectFields;
import query.main.select.element.classes.AllField;
import query.main.select.element.classes.FieldElementWithAlias;
import query.main.select.element.classes.FieldSelectedList;
import query.main.select.element.classes.SelectCtx;


public class ProjectionHelper {
    Relation result;
    Relation src;
    SelectFields field;
    SelectCtx ctx;

    public ProjectionHelper(Relation src, SelectFields field,SelectCtx ctx) {
        this.src = src;
        this.ctx=ctx;
        this.field = field;
        this.result = new Relation();
    }

    public Vector<QualifiedIdentifier> makeFieldName() {
        if (field instanceof FieldSelectedList list) {
            Vector<QualifiedIdentifier> newFieldName = new Vector<>();
            for (FieldElementWithAlias field : list) {
                if (field.getExpr() instanceof PrimitiveExpr maybeQid && field.getAlias() == null) {
                    if (maybeQid.getValue() instanceof QualifiedIdentifier qid) {
                        newFieldName.add(qid);
                    }else{
                        newFieldName.add(new QualifiedIdentifier(null, field.getExpr().toString()));     
                    }
                } else if (field.getAlias() != null) {
                    newFieldName.add(new QualifiedIdentifier(null, field.getAlias()));
                } else {
                    newFieldName.add(new QualifiedIdentifier(null, field.getExpr().toString()));
                }
            }

            return newFieldName;
        } else {
            throw new IllegalArgumentException(
                    "Field is not an instance of AllField but may be a FieldElementWithAlias or something else");
        }
    }

    public Vector<Domain> makeDomains() throws AmbigousNameErr, FieldNotFoundErr, AmbigousAliasErr {
        if (field instanceof FieldSelectedList list) {
            Vector<Domain> newDomains = new Vector<>();
            for (FieldElementWithAlias field : list) {
                if (field.getExpr() instanceof PrimitiveExpr maybeQid) {
                    if (maybeQid.getValue() instanceof QualifiedIdentifier qid) {
                        int index = qid.getIndex(src.getFieldName(),ctx);
                        newDomains.add(src.getDomaines().get(index));
                    }
                } else {
                    newDomains.add(Domain.makeUniversalDomain());
                }
            }
            return newDomains;
        } else {
            throw new IllegalArgumentException(
                    "Field is not an instance of AllField but may be a FieldElementWithAlias or something else");
        }
    }

    public Vector<Object> projectIndividual(Vector<Object> row) throws EvalErr {
        Vector<Object> projectedRow = new Vector<>();

        if (field instanceof FieldSelectedList list) {
            for (FieldElementWithAlias fieldElement : list) {

                Object value = null;
                if (fieldElement.getExpr() instanceof PrimitiveExpr primitiveExpr) {
                    Object primitiveValue = primitiveExpr.getValue();

                    if (primitiveValue instanceof QualifiedIdentifier qid) {
                        value = qid.getValueFromARow(src.getFieldName(), row,ctx);
                    } else {
                        value = primitiveValue;
                    }
                } else {
                    value = fieldElement.getExpr().eval(src, row,ctx);
                }

                projectedRow.add(value);
            }
        } else {
            throw new IllegalArgumentException(
                    "Unsupported field type: " + field.getClass().getName());
        }

        return projectedRow;
    }

    public Relation executeProjectionForList() throws EvalErr {
        this.result.setName(src.getName() + "_projection");
        this.result.setDomaines(makeDomains());
        this.result.setFieldName(makeFieldName());

        for (Vector<Object> individu : src.getIndividus()) {
            this.result.getIndividus().add(projectIndividual(individu));
        }
        return this.result;
    }

    public Relation executeProjection() throws EvalErr {
        if(this.field instanceof AllField){
            return src;
        }else{
            return executeProjectionForList();
        }
    }

}
