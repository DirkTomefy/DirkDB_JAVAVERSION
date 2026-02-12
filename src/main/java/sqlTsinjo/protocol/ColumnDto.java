package sqlTsinjo.protocol;

public class ColumnDto {
    public String name;
    public String origin;

    public ColumnDto() {
    }

    public ColumnDto(String name, String origin) {
        this.name = name;
        this.origin = origin;
    }
}
