package sqlTsinjo.query.main.select.element.abstracts;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.AmbigousAliasErr;
import sqlTsinjo.query.err.parsing.token.TokenNotFound;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.SelectRqst;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;
import sqlTsinjo.query.main.select.element.classes.TableNameOrigin;
import sqlTsinjo.query.main.select.element.err.AliasNeededException;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
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
        ParseSuccess<TableOriginWithAlias> origin = parseFromWithoutAlias(input);
        ParseSuccess<String> alias = parseOptionalAlias(origin.remaining());
        if (alias.matched() == null && origin.matched() instanceof SelectRqst) {
            throw new AliasNeededException(alias.matched());
        } else {
            origin.matched().setAlias(alias.matched());
            return new ParseSuccess<TableOriginWithAlias>(alias.remaining(), origin.matched());
        }
    }

    public static ParseSuccess<TableOriginWithAlias> parseFromWithoutAlias(String input) throws ParseNomException {
        if (Tokenizer.startsWithFactor(input)) {
            ParseSuccess<Token> parens_token = Tokenizer.tagParensToken().apply(input.trim());
            ParseSuccess<TableOriginWithAlias> origin = parseFromWithoutAlias(parens_token.remaining());
            ParseSuccess<Token> parens_token1 = Tokenizer.tagParensToken().apply(origin.remaining().trim());
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

    public static ParseSuccess<String> parseOptionalAlias(String input) {
        return ParserNomUtil.opt(ParserNomUtil.alt(
            TableOriginWithAlias::parseExplicitAlias,
            TableNameOrigin::parseImplicitAlias
        ), input);
    }

    //implicit version : table alias
    public static ParseSuccess<String> parseImplicitAlias(String input) throws ParseNomException{
        return ParserNomUtil.tagName(input.trim());
    }

    //explicit version : table as alias
    public static ParseSuccess<String> parseExplicitAlias(String input) throws ParseNomException{
        ParseSuccess<Token> aliasSign=SelectTokenizer.scanAsToken(input);
        return ParserNomUtil.tagName(aliasSign.remaining().trim());
    }

    public abstract Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException, EvalErr,  IOException ;

    public Relation evalAsTableOriginAndHandleId(SelectCtx context) throws ParseNomException, EvalErr, IOException{
        Relation rel=evalAsTableOrigin0(context);
        for (QualifiedIdentifier colum : rel.getFieldName() ) {
            colum.setOrigin(id);
        }
        return rel;
    }

    public abstract void makeAliasAsTableOrigin(LinkedHashMap<String,String> aliasMap) throws AmbigousAliasErr;

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