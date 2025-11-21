package base;

public abstract class DomainAtom {
    private boolean canBenull = false;

    public abstract boolean isSupportable(Object value);

    public void setCanBenull(boolean canBenull) {
        this.canBenull = canBenull;
    }

    public boolean getCanBenull() {
        return this.canBenull;
    }

    public Domain intoDomain() {
        Domain a = new Domain();
        a.append(this);
        return a;
    }
}
