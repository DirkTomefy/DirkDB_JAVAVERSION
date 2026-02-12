package sqlTsinjo.protocol;

public class RelationPageDto {
    public String type;
    public String name;
    public int page;
    public int totalPages;
    public ColumnDto[] columns;
    public Object[][] rows;

    public RelationPageDto() {
    }
}
