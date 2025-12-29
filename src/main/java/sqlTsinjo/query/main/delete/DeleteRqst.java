package sqlTsinjo.query.main.delete;

import java.io.IOException;

import sqlTsinjo.base.Relation;
import sqlTsinjo.base.err.EvalErr;
import sqlTsinjo.base.err.ParseNomException;
import sqlTsinjo.cli.AppContext;
import sqlTsinjo.query.base.ParseSuccess;
import sqlTsinjo.query.base.classes.expr.Expression;
import sqlTsinjo.query.main.delete.token.DeleteRqstTokenizer;
import sqlTsinjo.query.main.select.SelectRqst;
import sqlTsinjo.query.token.Token;
import sqlTsinjo.storage.SerdeRelation;

public class DeleteRqst {
    String tableName;
    Expression where;
    public DeleteRqst(String tableName, Expression where) {
        this.tableName = tableName;
        this.where = where;
    }
    public static ParseSuccess<DeleteRqst> parseDelete(String input) throws ParseNomException{
        ParseSuccess<Token> deleteIndicator = DeleteRqstTokenizer.scanDeleteToken(input);
        ParseSuccess<Expression> expr = SelectRqst.parseOptionalWhere(deleteIndicator.remaining());
        return new ParseSuccess<DeleteRqst>(expr.remaining(), new DeleteRqst((String) deleteIndicator.matched().value, expr.matched() ));
    }
    public  void eval(AppContext ctc) throws IOException, ParseNomException, EvalErr {
        SerdeRelation serde = new SerdeRelation(ctc,this.tableName );
        Relation rel=serde.deserializeRelation();
        rel.delete(where);
        serde.serializeRelation(rel);
    }
    public String getTableName() {
        return tableName;
    }
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public Expression getWhere() {
        return where;
    }
    public void setWhere(Expression where) {
        this.where = where;
    }
}
