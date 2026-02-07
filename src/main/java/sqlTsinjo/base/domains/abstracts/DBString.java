package sqlTsinjo.base.domains.abstracts;

import sqlTsinjo.base.DomainAtom;

public abstract class DBString<T> extends DomainAtom {
    private  Class<T> clazz;

    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected abstract String intoStringValue0(T value);
    
    public String intoStringValue(Object value) {
        if (clazz.isInstance(value)) {
            T typedValue = clazz.cast(value);
            return intoStringValue0(typedValue);
        } else {
            return null;
        }
    }

}
