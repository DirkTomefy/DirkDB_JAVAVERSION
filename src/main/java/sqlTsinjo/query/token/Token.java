package sqlTsinjo.query.token;

import sqlTsinjo.query.base.classes.operand.BinaryOp;
import sqlTsinjo.query.base.classes.operand.PrefixedOp;
import sqlTsinjo.query.main.common.QualifiedIdentifier;
import sqlTsinjo.query.main.select.element.enums.BasicRowOp;
import sqlTsinjo.query.main.select.element.enums.JoinOp;
import sqlTsinjo.query.main.sqlobject.ObjectSQLEnum;

public class Token {
    public TokenKind status;
    public Object value;

    public Token(TokenKind status, Object value) {
        this.status = status;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public static Token number(double v) {
        return new Token(TokenKind.NUMBER, v);
    }

    public static Token id(QualifiedIdentifier f) {
        return new Token(TokenKind.ID, f);
    }

    public static Token string(String s) {
        return new Token(TokenKind.STRING, s);
    }

    public static Token binop(BinaryOp op) {
        return new Token(TokenKind.BINOP, op);
    }

    public static Token other(String other) {
        return new Token(TokenKind.OTHER, other);
    }

    public static Token prefixedop(PrefixedOp op) {
        return new Token(TokenKind.PREFIXEDOP, op);
    }

    public static Token nullvalue() {
        return new Token(TokenKind.NULLVALUE, null);
    }

    public static Token selectSign() {
        return new Token(TokenKind.SELECT, null);
    }

    public static Token comma() {
        return new Token(TokenKind.COMMA, null);
    }

    public static Token asSign() {
        return new Token(TokenKind.AS, null);
    }

    public static Token rightJoin() {
        return new Token(TokenKind.JOIN, JoinOp.RIGHT);
    }

    public static Token leftJoin() {
        return new Token(TokenKind.JOIN, JoinOp.LEFT);
    }

    public static Token innerJoin() {
        return new Token(TokenKind.JOIN, JoinOp.INNER);
    }

    public static Token fullJoin() {
        return new Token(TokenKind.JOIN, JoinOp.FULL);
    }

    public static Token naturalJoin() {
        return new Token(TokenKind.JOIN, JoinOp.NATURAL);
    }

    public static Token union() {
        return new Token(TokenKind.RELATIONOP, BasicRowOp.UNION);
    }

    public static Token intersection() {
        return new Token(TokenKind.RELATIONOP, BasicRowOp.INTERSECTION);
    }

    public static Token difference() {
        return new Token(TokenKind.RELATIONOP, BasicRowOp.DIFFERENCE);
    }

    public static Token createDatabase() {
        return new Token(TokenKind.CREATEDATABASE, null);
    }

    public static Token createTable() {
        return new Token(TokenKind.CREATETABLE, null);
    }

       public static Token createDomain() {
        return new Token(TokenKind.CREATEDOMAIN, null);
    }

    public static Token useDatabase(String databaseName) {
        return new Token(TokenKind.USEDATABASE, databaseName);
    }
    public static Token insertSign(){
        return new Token(TokenKind.INSERTINTO, null );
    }
    public static Token updateSign(String tableName){
        return new Token(TokenKind.UPDATE, tableName );
    }
    public static Token deleteSign(String tableName){
        return new Token(TokenKind.DELETE , tableName);
    }
    public static Token dropObjectSQL(ObjectSQLEnum type){
        return new Token(TokenKind.DROPOBJECTSQL, type);
    }

    public static Token showListObjectSQL(ObjectSQLEnum type){
        return new Token(TokenKind.SHOW, type);
    }

    @Override
    public String toString() {
        return value == null ? status.name()
                : status.name() + "(" + value.getClass().getSimpleName() + "." + value + ")";
    }

}
