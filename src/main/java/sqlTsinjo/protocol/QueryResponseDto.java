package sqlTsinjo.protocol;

public class QueryResponseDto {
    public String message;
    public RelationDto relation;

    public QueryResponseDto() {
    }

    public static QueryResponseDto withMessage(String message) {
        QueryResponseDto r = new QueryResponseDto();
        r.message = message;
        return r;
    }

    public static QueryResponseDto withRelation(RelationDto relation, String message) {
        QueryResponseDto r = new QueryResponseDto();
        r.relation = relation;
        r.message = message;
        return r;
    }
}
