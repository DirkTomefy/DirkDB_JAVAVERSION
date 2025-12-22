package query.main;

public class FieldName {
    public FieldName(String src, String simplename) {
        this.src = src;
        this.simplename = simplename;
    }
    String src;
    String simplename;
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
