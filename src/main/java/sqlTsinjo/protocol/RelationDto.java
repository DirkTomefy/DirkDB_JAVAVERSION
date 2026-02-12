package sqlTsinjo.protocol;

import java.util.ArrayList;
import java.util.List;

public class RelationDto {
    public String name;
    public List<ColumnDto> columns = new ArrayList<>();
    public List<List<Object>> rows = new ArrayList<>();

    public RelationDto() {
    }
}
