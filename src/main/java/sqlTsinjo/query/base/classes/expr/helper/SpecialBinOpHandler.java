package sqlTsinjo.query.base.classes.expr.helper;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;

@FunctionalInterface
public interface SpecialBinOpHandler {
    ParseSuccess<Expression> handle(Expression left, String input) throws ParseNomException;    
}
