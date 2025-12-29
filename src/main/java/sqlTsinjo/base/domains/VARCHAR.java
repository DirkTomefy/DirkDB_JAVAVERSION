package sqlTsinjo.base.domains;

import sqlTsinjo.base.domains.abstracts.DBString;

public class VARCHAR extends DBString<String> {
    Integer min;
    Integer limit;

    public VARCHAR() {
    }

    public VARCHAR(Integer min, Integer limit) {
        this.min = min;
        this.limit = limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public VARCHAR(Integer limit) {
        this.setClazz(String.class);
        this.setLimit(limit);
    }

    public VARCHAR(boolean canBenull, Integer limit) {
        this.setClazz(String.class);
        this.setCanBenull(canBenull);
        this.setLimit(limit);
    }

    @Override
    public boolean isSupportable(Object value) {
        // Si la valeur est null
        if (value == null) 
            return Boolean.TRUE.equals(this.getCanBenull()); // autorisé si canBeNull est true
        

        if (value instanceof char[] c) 
            value = new String(c);
        

        // Vérifie que c'est bien une String
        if (!(value instanceof String s)) {
            return false;
        }

        // Vérifie la taille minimale si définie
        if (min != null && s.length() < min) {
            return false;
        }

        // Vérifie la taille maximale si définie
        if (limit != null && s.length() > limit) {
            return false;
        }

        // Tout est OK
        return true;
    }

    @Override
    public String toString() {
        return "VARCHAR( " + min + "," + limit + " )";
    }

    @Override
    public String intoStringValue0(String value) {
        return value;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    public Integer getLimit() {
        return limit;
    }
}
