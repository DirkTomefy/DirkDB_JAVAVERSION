package sqlTsinjo.socket.server;

public class RelationPageDto {
    public final String type = "relation";
    public final String name;
    public final int page;
    public final int totalPages;
    public final ColumnDto[] columns;
    public final Object[][] rows;

    public RelationPageDto(String name, int page, int totalPages, ColumnDto[] columns, Object[][] rows) {
        this.name = name;
        this.page = page;
        this.totalPages = totalPages;
        this.columns = columns;
        this.rows = rows;
    }
}
