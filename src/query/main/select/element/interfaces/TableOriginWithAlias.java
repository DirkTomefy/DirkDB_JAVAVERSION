package query.main.select.element.interfaces;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.err.parsing.token.TokenNotFound;
import query.main.select.SelectRqst;
import query.main.select.element.err.AliasNeededException;
import query.token.Token;
import query.token.Tokenizer;

public abstract class TableOriginWithAlias {
    protected String id;
    protected String alias;

    public TableOriginWithAlias(String id, String alias) {
        this.id = id;
        this.alias = alias;
    }

    public TableOriginWithAlias() {
    }

    public static ParseSuccess<TableOriginWithAlias> parseTableOrigin(String input) throws ParseNomException {
        ParseSuccess<TableOriginWithAlias> origin = parseFromBase0(input);
        ParseSuccess<String> alias = parseOptionaAlias(origin.remaining());
        if (alias.matched() == null && origin.matched() instanceof SelectRqst) {
            throw new AliasNeededException(alias.matched());
        } else {
            // !! trés important :
            origin.matched().setAlias(alias.matched());
            return new ParseSuccess<TableOriginWithAlias>(alias.remaining(), origin.matched());
        }
    }

    public static ParseSuccess<TableOriginWithAlias> parseFromBase0(String input) throws ParseNomException {
        // TODO : générer l'unique id : 
        String id = "fjdsmkfj124";
        if (Tokenizer.startsWithFactor(input)) {
            ParseSuccess<Token> parens_token = Tokenizer.tagParensToken().apply(input);
            ParseSuccess<TableOriginWithAlias> origin = parseFromBase0(parens_token.remaining());

            ParseSuccess<Token> parens_token1 = Tokenizer.tagParensToken().apply(origin.remaining());
            if (parens_token1.matched().value.equals(")")) {
                // !! trés important :
                origin.matched().setId(id);
                return new ParseSuccess<TableOriginWithAlias>(parens_token1.remaining(), origin.matched());
            } else {
                throw new TokenNotFound(origin.remaining());
            }
        } else {
            // TODO : vérifier si c'est une table ou un subSelect
        }
        return null;
    }

    public static ParseSuccess<String> parseOptionaAlias(String input) {
        // TODO 
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}