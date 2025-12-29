package sqlTsinjo.query.main.update;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.main.select.SelectRqst;
import sqlTsinjo.query.main.update.token.UpdateRqstTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeRelation;

public class UpdateRqst {
    String tableName;
    HashMap<String, Expression> newValues;
    Expression expr;

    public UpdateRqst(String tableName, HashMap<String, Expression> newValues, Expression expr) {
        this.tableName = tableName;
        this.newValues = newValues;
        this.expr = expr;
    }
    public void eval(AppContext ctx) throws IOException, ParseNomException, EvalErr{
        SerdeRelation serde=new SerdeRelation(ctx, tableName);
        Relation rel=serde.deserializeRelation();
        rel.update(newValues, expr);
        //TODO : transaction
        serde.serializeRelation(rel);
    }

    public static ParseSuccess<UpdateRqst> parseUpdate(String input) throws ParseNomException {
        ParseSuccess<Token> keywordAndTableName = UpdateRqstTokenizer.scanUpdateToken(input.trim());
        var t1 = UpdateRqstTokenizer.scanSetToken(keywordAndTableName.remaining().trim());
        ParseSuccess<HashMap<String, Expression>> set = UpdateRqst.parseValues(t1.remaining().trim());
        ParseSuccess<Expression> expr = SelectRqst.parseOptionalWhere(set.remaining());

        return new ParseSuccess<UpdateRqst>(expr.remaining(),
                new UpdateRqst((String) keywordAndTableName.matched().value, set.matched(), expr.matched() ));
    }

    public static ParseSuccess<HashMap<String, Expression>> parseValues(String input) throws ParseNomException {
        HashMap<String, Expression> values = new HashMap<>();
        String remaining = input.trim();

        // Parser la première paire
        ParseSuccess<Map.Entry<String, Expression>> firstPair = parseNameValuePair(remaining);
        values.put(firstPair.matched().getKey(), firstPair.matched().getValue());
        remaining = firstPair.remaining().trim();

        // Parser les paires suivantes séparées par des virgules
        while (remaining.startsWith(",")) {
            // Consommer la virgule
            remaining = remaining.substring(1).trim();

            if (remaining.isEmpty()) {
                throw new ParseNomException(remaining, "Paire name=expr attendue après la virgule");
            }

            // Parser la paire suivante
            ParseSuccess<Map.Entry<String, Expression>> nextPair = parseNameValuePair(remaining);
            Map.Entry<String, Expression> entry = nextPair.matched();
            values.put(entry.getKey(), entry.getValue());
            remaining = nextPair.remaining().trim();
        }

        return new ParseSuccess<>(remaining, values);
    }

    private static ParseSuccess<Map.Entry<String, Expression>> parseNameValuePair(String input)
            throws ParseNomException {
        String remaining = input.trim();

        // Parser le nom du champ
        ParseSuccess<String> nameParse = ParserNomUtil.tagName(remaining);
        String fieldName = nameParse.matched();
        remaining = nameParse.remaining().trim();

        // Vérifier le signe '='
        if (!remaining.startsWith("=")) {
            throw new ParseNomException(remaining, "Signe '=' attendu après le nom du champ");
        }

        // Consommer '='
        remaining = remaining.substring(1).trim();

        // Parser l'expression
        ParseSuccess<Expression> exprParse = Expression.parseExpression.apply(remaining);
        Expression expression = exprParse.matched();
        remaining = exprParse.remaining().trim();

        // Créer une Map.Entry
        Map.Entry<String, Expression> entry = new AbstractMap.SimpleEntry<>(fieldName, expression);
        return new ParseSuccess<>(remaining, entry);
    }

}
