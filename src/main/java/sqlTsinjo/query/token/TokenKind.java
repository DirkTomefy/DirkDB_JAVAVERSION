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
    CREATETABLE,
    CREATEDATABASE,

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
    SHOW
    ;
}
