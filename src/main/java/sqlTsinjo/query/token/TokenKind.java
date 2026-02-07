package sqlTsinjo.query.token;

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
    FROM,
    AS,
    JOIN,
    RELATIONOP,

    //CREATE :
    CREATEOBJECTSQL,
    // CREATEDATABASE,
    // CREATEDOMAIN,

    //USE :
    USEDATABASE,

    //INSERT :
    INSERTINTO,

    //UPDATE :
    UPDATE,

    //DELETE :
    DELETE,

    //DROP :
    DROPOBJECTSQL,

    //SHOW :
    SHOWOBJECTSQL
    ;
}
