package query.main.select.element.interfaces;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.helper.ParserNomUtil;
import query.main.select.element.classes.AllField;
import query.main.select.element.classes.FieldSelectedList;

public interface SelectFields {
    public static ParseSuccess<SelectFields> parse(String input) throws ParseNomException {
        if (input.trim().startsWith("*")) {
            ParseSuccess<String> success = ParserNomUtil.tag("*").apply(input.trim());
            return new ParseSuccess<SelectFields>(success.remaining().trim(), new AllField());
        }else{
           ParseSuccess<FieldSelectedList> success= parseFieldList(input);
           return new ParseSuccess<SelectFields>(success.remaining(),success.matched());
        }
    }
    public static ParseSuccess<FieldSelectedList> parseFieldList(String input){
        return null;
    }
}
