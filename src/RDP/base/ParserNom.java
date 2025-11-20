package RDP.base;

import RDP.err.ParseNomException;

@FunctionalInterface
public interface ParserNom<T> {
    public ParseSuccess<T> apply(String input) throws ParseNomException;
}
