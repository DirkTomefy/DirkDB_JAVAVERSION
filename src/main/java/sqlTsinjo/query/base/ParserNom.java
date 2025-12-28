package sqlTsinjo.query.base;

import sqlTsinjo.base.err.ParseNomException;

@FunctionalInterface
public interface ParserNom<T> {
    public ParseSuccess<T> apply(String input) throws ParseNomException;
}
