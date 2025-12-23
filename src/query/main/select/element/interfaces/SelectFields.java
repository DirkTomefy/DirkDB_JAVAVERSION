package query.main.select.element.interfaces;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.classes.expr.Expression;
import query.base.helper.ParserNomUtil;
import query.main.common.FieldSelectedList;
import query.main.select.element.classes.AllField;
import query.main.select.token.SelectTokenizer;
import query.token.Token;

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
        input=expr.remaining();
        FieldSelectedList fields = new FieldSelectedList();
        loop : while (true) {
            try (ParseSuccess<Token> t = SelectTokenizer.scanFieldsToken(input)) {
                switch (t.matched().status) {
                    case COMMA->{
                        // TODO
                    }
                    case ID->{
                        // TODO
                    }
                    case AS->{
                        // TODO
                    }
                    default->{
                        break loop;
                    }
                }
            }
        }
        return new ParseSuccess<FieldSelectedList>(input, fields);
    }
}
