package query.token;

public enum TokenKind {
    NUMBER,
    ID,
    STRING,
    BINOP,
    PREFIXEDOP,
    OTHER,
    NULLVALUE,
    // ---OTHER TOKEN KIND ::
    SELECT,
    COMMA,
    FROM
    ;
}
