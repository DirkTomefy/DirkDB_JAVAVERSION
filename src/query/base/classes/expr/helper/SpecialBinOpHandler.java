package query.base.classes.expr.helper;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;

@FunctionalInterface
public interface SpecialBinOpHandler {
    ParseSuccess<Expression> handle(Expression left, String input) throws ParseNomException;    
}
