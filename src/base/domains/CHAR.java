package base.domains;

import base.DomainAtom;
import base.domains.interfaces.DBString;

public class CHAR extends DomainAtom implements DBString<char[]> { 
    int limit;
    
    public CHAR(int limit) {
        this.limit = limit;
    }

    @Override
    public String intoStringValue(char[] value) {
        return new String(value);
    }

    @Override
    public boolean isSupportable(Object value) {
        if(value instanceof char[] mychars){
            if(mychars.length<=limit) return true;
            return false;
        }else{
            return false;
        }
    }
}
