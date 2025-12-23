package base.util;

import java.util.Vector;

import base.Relation;
import query.err.eval.FieldNotFoundErr;
import query.err.eval.FieldToProjectEmpty;
import query.main.common.QualifiedIdentifier;

public class ProjectionHelper {
    private QualifiedIdentifier[] fields;
    private Relation source;
    private Relation result;
    private Vector<Integer> fieldIndices;

    public Relation executeProjection(Relation source, QualifiedIdentifier[] fields) throws FieldNotFoundErr, FieldToProjectEmpty {
        if(fields==null) throw new FieldToProjectEmpty();
        if(fields.length<1) throw new FieldToProjectEmpty();
        this.source = source;
        this.fields = fields;

        validateFields();
        initializeResult();
        setupMetadata();
        projectData();
        return result;
    }

    private void validateFields() throws FieldNotFoundErr {
        for (QualifiedIdentifier field : fields) {
            if (!source.getFieldName().contains(field)) {
                throw new FieldNotFoundErr(field);
            }
        }
    }

    private void initializeResult() {
        result = new Relation();
        result.setName(source.getName() + "_projection");
        result.setFieldName(new Vector<>());
        result.setDomaines(new Vector<>());
        result.setIndividus(new Vector<>());
    }

    private void setupMetadata() throws FieldNotFoundErr {
        for (QualifiedIdentifier field : fields) {
            int index = source.getFieldName().indexOf(field);
            if(index==-1) throw new FieldNotFoundErr(field);
            result.getFieldName().add(source.getFieldName().get(index));
        }
    }

    private void projectData() {
        calculateFieldIndices();

        for ( Vector<Object> individu : source.getIndividus()) {
             Vector<Object> projected = projectIndividual(individu);
            result.appendIfNotExist(projected);
        }
    }

    private void calculateFieldIndices() {
        fieldIndices = new Vector<>();
        for (QualifiedIdentifier field : fields) {
            fieldIndices.add(source.getFieldName().indexOf(field));
        }
    }

    private  Vector<Object> projectIndividual( Vector<Object> original) {
         Vector<Object> projected = new  Vector<Object>();

        for (int fieldIndex : fieldIndices) {
            Object value = original.get(fieldIndex);
            projected.add(value);
        }
        return projected;
    }

}
