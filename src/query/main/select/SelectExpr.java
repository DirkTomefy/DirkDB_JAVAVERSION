package query.main.select;

import base.Relation;
import base.err.EvalErr;
import base.err.ParseNomException;
import cli.AppContext;
import query.base.ParseSuccess;
import query.base.helper.ParserNomUtil;
import query.main.select.element.enums.BasicRowOp;
import query.main.select.token.SelectTokenizer;
import query.token.Token;
import query.token.Tokenizer;

public interface SelectExpr {
    public Relation eval(AppContext context) throws ParseNomException, EvalErr ;
    public static ParseSuccess<SelectExpr> parseExpr(String input) throws ParseNomException {
        return parseBinaryExpr(input);
    }

    private static ParseSuccess<SelectExpr> parseBinaryExpr(String input) throws ParseNomException {
        ParseSuccess<SelectExpr> left = parseFactor(input);
        input = left.remaining();

        SelectExpr current = left.matched();

        while (true) {
            if (Tokenizer.codonStop(input))
                break;
            ParseSuccess<Token> next = ParserNomUtil.opt(SelectTokenizer::scanBasicRelationalOpToken, input);
            if (next.matched() == null)
                break;
            input = next.remaining();
            BasicRowOp token = (BasicRowOp) next.matched().value;
            ParseSuccess<SelectExpr> rhs = parseFactor(input);
            current = new SelectBinOpExpr(current, token, current);
            input = rhs.remaining();

        }
        return new ParseSuccess<>(input, current);
    }

    public static ParseSuccess<SelectExpr> parseFactor(String input) throws ParseNomException {
        ParseSuccess<Token> beginParens = ParserNomUtil.optParser(Tokenizer.tagParensToken()).apply(input);
        if (beginParens.matched() == null) {
            ParseSuccess<SelectRqst> rqst = SelectRqst.parseSelect(input);
            return new ParseSuccess<SelectExpr>(rqst.remaining(), rqst.matched());
        } else if ("(".equals(beginParens.matched().value)) {
            ParseSuccess<SelectExpr> inner = parseExpr(beginParens.remaining());
            ParseSuccess<Token> endParens = ParserNomUtil.optParser(Tokenizer.tagParensToken())
                    .apply(inner.remaining());
            if (endParens != null && ")".equals(endParens.matched().value)) {
                return new ParseSuccess<SelectExpr>(endParens.remaining(), inner.matched());
            } else {
                throw new ParseNomException(input, "Parenthèse fermante exécépté à la place :" + endParens.matched());
            }
        } else {
            throw new ParseNomException(input, "Parenthèse fermante détécté au lieu de ouverte");
        }

    }

}
