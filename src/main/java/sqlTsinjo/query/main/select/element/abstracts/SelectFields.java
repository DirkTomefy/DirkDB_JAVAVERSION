package sqlTsinjo.query.main.select.element.abstracts;

import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.SelectRqst;
import sqlTsinjo.query.main.select.element.classes.AllField;
import sqlTsinjo.query.main.select.element.classes.FieldElementWithAlias;
import sqlTsinjo.query.main.select.element.classes.FieldSelectedList;
import sqlTsinjo.query.main.select.element.err.AliasWithSourceCodeException;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

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
            ParseSuccess<Token> t = ParserNomUtil.opt(SelectTokenizer::scanFieldsToken, input.trim());
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
                        ParseSuccess<QualifiedIdentifier> q = ParserNomUtil.identifier1(input.trim());
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
        if (q.getOrigin() == null) {
            fields.getLast().setAlias(q.getName());
        } else {
            throw new AliasWithSourceCodeException(input);
        }
    }

    public static void main(String[] args) throws ParseNomException {
        System.out.println("select : \n" + SelectRqst.parseSelect("alaivo 1+1 antso u3 , t1.u1  l #ao@ ( alaivo * #ao@ table_etoile ) t1 #tonona:avia@ t2"));
    }
}
