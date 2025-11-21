package base.domains;

import base.DomainAtom;
import base.domains.interfaces.DBString;

public class VARCHAR extends DomainAtom implements DBString<String>{
    Integer limit;

    public void setLimit(Integer limit) {
        this.limit = limit;
    }


    public VARCHAR(Integer limit){
        this.setLimit(limit);
    }

    public VARCHAR(boolean canBenull,Integer limit){
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
    public String intoStringValue(String value) {
        return value;
    }
}
