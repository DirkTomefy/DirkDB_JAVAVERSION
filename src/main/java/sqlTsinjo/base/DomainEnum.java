package sqlTsinjo.base;

import java.util.HashSet;

public class DomainEnum extends DomainAtom {
    HashSet<Object> allowedValue;

    public DomainEnum() {
    }

    public DomainEnum(boolean canBenull, HashSet<Object> allowedValue) {
        this.allowedValue = allowedValue;
    }

    public DomainEnum(HashSet<Object> allowedValue) {
        this.allowedValue = allowedValue;
    }

    @Override
    public boolean isSupportable(Object value) {
        return this.allowedValue.contains(value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DomainEnum{");
        sb.append("allowedValues=[");
        
        if (allowedValue != null && !allowedValue.isEmpty()) {
            int count = 0;
            for (Object value : allowedValue) {
                if (count > 0) {
                    sb.append(", ");
                }
                sb.append(value.toString());
                count++;
                if (count >= 10) {
                    sb.append(", ...");
                    break;
                }
            }
        } else {
            sb.append("empty");
        }
        
        sb.append("], size=").append(allowedValue != null ? allowedValue.size() : 0);
        sb.append("}");
        
        return sb.toString();
    }

    public HashSet<Object> getAllowedValue() {
        return allowedValue;
    }

    public void setAllowedValue(HashSet<Object> allowedValue) {
        this.allowedValue = allowedValue;
    }
}
