package sqlTsinjo.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class DomainAtom {
    private boolean canBenull = true;

    public abstract boolean isSupportable(Object value);

    public void setCanBenull(boolean canBenull) {
        this.canBenull = canBenull;
    }

    public boolean getCanBenull() {
        return this.canBenull;
    }

    public Domain intoDomain() {
        Domain a = new Domain();
        a.add(this);
        return a;
    }
}
