package query.token;

import query.base.classes.operand.BinaryOp;
import query.base.classes.operand.PrefixedOp;
import query.main.common.QualifiedIdentifier;

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
        return new Token( TokenKind.PREFIXEDOP, op );
    }
    public static Token nullvalue(){
        return new Token(TokenKind.NULLVALUE, null);
    }
    public static Token selectSign(){
        return new Token(TokenKind.SELECT, null);
    }
    @Override
    public String toString() {
        return value == null ? status.name() : status.name() + "("+value.getClass().getSimpleName()+"." + value + ")";
    }


}
