package base.domains;
import base.domains.abstracts.DBString;

public class VARCHAR extends DBString<String>{
    Integer limit;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }


    public VARCHAR(Integer limit){
        this.setClazz(String.class);
        this.setLimit(limit);
    }

    public VARCHAR(boolean canBenull,Integer limit){
        this.setClazz(String.class);
        this.setCanBenull(canBenull);
        this.setLimit(limit);
    }
    
    @Override
    public boolean isSupportable(Object value) {
        
        if(value==null) return false;
        if (value instanceof String s) {
            if (limit == null) {
                return true;
            } else {
                return s.length() <= limit;
            }
        } else {
            return false;
        }
    }


    @Override
    public String toString(){
        return "VARCHAR( "+limit+" )";
    }


    @Override
    public String intoStringValue0(String value) {
        return value;
    }
}
