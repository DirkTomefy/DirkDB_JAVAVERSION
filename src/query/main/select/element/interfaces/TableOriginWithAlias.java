package query.main.select.element.interfaces;

import base.err.ParseNomException;
import query.base.ParseSuccess;
import query.base.helper.ParserNomUtil;
import query.err.parsing.token.TokenNotFound;
import query.main.select.SelectRqst;
import query.main.select.element.classes.TableNameOrigin;
import query.main.select.element.err.AliasNeededException;
import query.main.select.token.SelectTokenizer;
import query.token.Token;
import query.token.Tokenizer;
import java.util.UUID;

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
            origin.matched().setAlias(alias.matched());
            return new ParseSuccess<TableOriginWithAlias>(alias.remaining(), origin.matched());
        }
    }

    public static ParseSuccess<TableOriginWithAlias> parseFromBase0(String input) throws ParseNomException {
        if (Tokenizer.startsWithFactor(input)) {
            ParseSuccess<Token> parens_token = Tokenizer.tagParensToken().apply(input);
            ParseSuccess<TableOriginWithAlias> origin = parseFromBase0(parens_token.remaining());

            ParseSuccess<Token> parens_token1 = Tokenizer.tagParensToken().apply(origin.remaining());
            if (parens_token1.matched().value.equals(")")) {
                return new ParseSuccess<TableOriginWithAlias>(parens_token1.remaining(), origin.matched());
            } else {
                throw new TokenNotFound(origin.remaining());
            }
        } else {
            ParseSuccess<Token> select_sign = ParserNomUtil.opt(SelectTokenizer::scanSelectToken, input);
            if (select_sign.matched() != null) {
                // * ICI CELA EST UN SUBSELECT*/
                ParseSuccess<SelectRqst> select = SelectRqst.parseSelect(input);
                select.matched().setAlias(null);
                select.matched().setId(UUID.randomUUID().toString());
                return new ParseSuccess<TableOriginWithAlias>(select.remaining(), select.matched());
            } else {
                // * ICI CELA EST UN NOM DE TABLE*/
                ParseSuccess<String> tableName = ParserNomUtil.tagName(input.trim());
                return new ParseSuccess<TableOriginWithAlias>(tableName.remaining(),
                        new TableNameOrigin(UUID.randomUUID().toString(), null, tableName.matched()));
            }
        }
    }

    public static ParseSuccess<String> parseOptionaAlias(String input) {
        // TODO : générer les alias optionel
        return new ParseSuccess<String>(input, null);
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