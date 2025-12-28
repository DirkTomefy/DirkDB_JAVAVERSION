package sqlTsinjo.base.domains;

import java.util.Date;

import sqlTsinjo.base.DomainAtom;

public class DATE extends DomainAtom{

    @Override
    public boolean isSupportable(Object value) {
        return Date.class.isAssignableFrom(value.getClass());
    }
    
    @Override
    public String toString(){
        return "DATE";
    }
}
