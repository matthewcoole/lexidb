package query.json;

import lombok.Data;

@Data
public class SortProperty {
    private String column;
    private int position;
    private boolean alphabetical, ascending;
}
