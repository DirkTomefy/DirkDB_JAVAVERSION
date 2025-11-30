package RDP.base;

import base.err.ParseNomException;

@FunctionalInterface
public interface ParserNom<T> {
    public ParseSuccess<T> apply(String input) throws ParseNomException;
}
