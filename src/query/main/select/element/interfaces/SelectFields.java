package query.main.select.element.interfaces;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.main.common.FieldSelectedList;
import query.main.common.QualifiedIdentifier;
import query.main.select.SelectRqst;
import query.main.select.element.classes.AllField;
import query.main.select.element.classes.FieldElementWithAlias;
import query.main.select.element.err.AliasWithSourceCodeException;
import query.main.select.token.SelectTokenizer;
import query.token.Token;
import query.token.Tokenizer;

public interface SelectFields {
    public static ParseSuccess<SelectFields> parse(String input) throws ParseNomException {
        if (input.trim().startsWith("*")) {
            ParseSuccess<String> success = ParserNomUtil.tag("*").apply(input.trim());
            return new ParseSuccess<SelectFields>(success.remaining().trim(), new AllField());
        } else {
            ParseSuccess<FieldSelectedList> success = parseFieldList(input);
            return new ParseSuccess<SelectFields>(success.remaining(), success.matched());
        }
    }

    public static ParseSuccess<FieldSelectedList> parseFieldList(String input) throws ParseNomException {
        ParseSuccess<Expression> expr = Expression.parseExpression.apply(input);
        input = expr.remaining();
        FieldSelectedList fields = new FieldSelectedList(expr.matched());
        loop: while (true) {
            ParseSuccess<Token> t = ParserNomUtil.opt(SelectTokenizer::scanFieldsToken, input);
            if (t.matched() == null) {
                break;
            } else {
                input = t.remaining();
                switch (t.matched().status) {

                    // après une virgule : nouvelle expression
                    case COMMA -> {
                        ParseSuccess<Expression> expr_after_comma = Expression.parseExpression.apply(input);
                        input = expr_after_comma.remaining();
                        fields.add(new FieldElementWithAlias(expr_after_comma.matched(), null));
                    }

                    // alias implicite : SELECT name username
                    case ID -> {
                        QualifiedIdentifier q = (QualifiedIdentifier) t.matched().value;
                        handleAliasForLast(fields, q, input);
                    }

                    // alias explicite avec AS : SELECT name AS username
                    case AS -> {
                        ParseSuccess<QualifiedIdentifier> q = ParserNomUtil.identifier1().apply(input.trim());
                        input = q.remaining();
                        handleAliasForLast(fields, q.matched(), input);
                    }
                    default -> {
                        break loop;
                    }
                }
                if (Tokenizer.codonStop(input)) {
                    break loop;
                } else {
                    continue loop;
                }
            }

        }

        return new ParseSuccess<FieldSelectedList>(input, fields);
    }

    public static void handleAliasForLast(FieldSelectedList fields, QualifiedIdentifier q, String input)
            throws AliasWithSourceCodeException {
        if (q.origin() == null) {
            fields.getLast().setAlias(q.name());
        } else {
            throw new AliasWithSourceCodeException(input);
        }
    }

    public static void main(String[] args) throws ParseNomException {
        System.out.println("select : \n" + SelectRqst.parseSelect("alaivo 1+1 antso u3 , u1 ao@ table"));
    }
}
