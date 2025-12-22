package query.main;

public class FieldName {
    String src;
    String simplename;
    String alias;
    public FieldName(String src, String simplename, String alias) {
        this.src = src;
        this.simplename = simplename;
        this.alias = alias;
    }
    public String getAlias() {
        return alias;
    }
    public void setAlias(String alias) {
        this.alias = alias;
    }
    public String getSrc() {
        return src;
    }
    public void setSrc(String src) {
        this.src = src;
    }
    public String getSimplename() {
        return simplename;
    }
    public void setSimplename(String simplename) {
        this.simplename = simplename;
    }
}
