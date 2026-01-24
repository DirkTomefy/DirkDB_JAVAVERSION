package sqlTsinjo.query.result;

import java.util.Optional;

import sqlTsinjo.base.Relation;

public class RequestResult {
    private final Optional<Relation> relation;

    // autre message (mais pas d'érreur)
    private final String message;

    private final boolean debugMode;

    public RequestResult(Optional<Relation> relation, String message, boolean debugMode) {
        this.relation = relation == null ? Optional.empty() : relation;
        this.message = message;
        this.debugMode = debugMode;
    }

    public static RequestResult withRelation(Relation relation, boolean debugMode) {
        return new RequestResult(Optional.ofNullable(relation), null, debugMode);
    }

    public static RequestResult withMessage(String message) {
        return new RequestResult(Optional.empty(), message, false);
    }

    public Optional<Relation> getRelation() {
        return relation;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        if (relation.isPresent()) {
            String output = debugMode
                    ? relation.get().toStringDebug()
                    : relation.get().toString();
            return "\n" + output + "\n";
        }

        return message == null ? "" : message;
    }
}
