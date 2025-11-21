package base.domains;
import base.domains.abstracts.DBString;

public class CHAR extends DBString<char[]> { 
    int limit;
    
    public CHAR(int limit) {
        this.setClazz(char[].class);
        this.limit = limit;
    }
    public CHAR(boolean canBenull,int limit){
        this.setClazz(char[].class);
        this.setCanBenull(canBenull);
        this.limit=limit;
    }
    @Override
    public String intoStringValue0(char[] value) {
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
