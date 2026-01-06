package sqlTsinjo.query.main.insert.element.abstracts;

import java.io.IOException;
import java.util.Vector;

import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.base.err.RelationalErr;
import sqlTsinjo.cli.AppContext;

public interface InsertRqstValues {
    public Vector<Vector<Object>> getMultiplyValues(AppContext context) throws RelationalErr, EvalErr, IOException,ParseNomException;
}
