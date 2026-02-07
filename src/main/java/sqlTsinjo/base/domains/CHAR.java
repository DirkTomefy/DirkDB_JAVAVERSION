package sqlTsinjo.base.domains;

import sqlTsinjo.base.domains.abstracts.DBString;

public class CHAR extends DBString<char[]> {
    int limit;

    public CHAR() {
    }

    public CHAR(int limit) {
        this.setClazz(char[].class);
        this.limit = limit;
    }

    public CHAR(boolean canBenull, int limit) {
        this.setClazz(char[].class);
        this.setCanBenull(canBenull);
        this.limit = limit;
    }

    @Override
    public String intoStringValue0(char[] value) {
        String v = new String(value);
        return v;
    }

    @Override
    public boolean isSupportable(Object value) {
        if (value instanceof char[] mychars) {
            if (mychars.length <= limit)
                return true;
            return false;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "CHAR( " + limit + " )";
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

}
