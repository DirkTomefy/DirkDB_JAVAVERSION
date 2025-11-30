package RDP.base.function.expr.helper;

import RDP.base.ParseSuccess;
import RDP.base.function.expr.Expression;
import base.err.ParseNomException;

@FunctionalInterface
public interface SpecialBinOpHandler {
    ParseSuccess<Expression> handle(Expression left, String input) throws ParseNomException;    
}
