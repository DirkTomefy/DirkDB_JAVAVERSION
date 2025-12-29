package sqlTsinjo.query.main.select;

import java.io.IOException;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.helper.ParserNomUtil;
import sqlTsinjo.query.err.eval.AmbigousAliasErr;
import sqlTsinjo.query.main.select.element.abstracts.TableOriginWithAlias;
import sqlTsinjo.query.main.select.element.classes.SelectCtx;
import sqlTsinjo.query.main.select.element.enums.BasicRowOp;
import sqlTsinjo.query.main.select.token.SelectTokenizer;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.query.token.Tokenizer;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class SelectExpr   extends TableOriginWithAlias  {

    public abstract Relation eval(AppContext context) throws ParseNomException, EvalErr, IOException ;

    public static ParseSuccess<SelectExpr> parseExpr(String input) throws ParseNomException {
        return parseBinaryExpr(input);
    }

    @Override
    public Relation evalAsTableOrigin0(SelectCtx context) throws ParseNomException, EvalErr, IOException {
        return this.eval(context.getAppcontext());
    }

    @Override
    public void makeAliasAsTableOrigin(LinkedHashMap<String, String> aliasMap) throws AmbigousAliasErr {
        if (alias!=null && aliasMap.containsKey(alias)) {
            throw new AmbigousAliasErr("Alias '" + alias + "' déjà utilisé");
        } else if(alias!=null){
            aliasMap.put(alias, id);
        }
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
            current = new SelectBinOpExpr(current, token, rhs.matched() );
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
