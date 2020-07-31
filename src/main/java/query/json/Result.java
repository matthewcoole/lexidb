package query.json;

import properties.AppProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Result {
    public int context = Integer.parseInt(AppProperties.get("kwic.context")),
            page,
            pageSize = Integer.parseInt(AppProperties.get("result.page.size")),
            n = 2;
    public String type = "kwic", groupby = "";
    public List<SortProperty> sort = new ArrayList<>();
    public boolean async = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Result result = (Result) o;
        return n == result.n
                && type.equals(result.type)
                && groupby.equals(result.groupby)
                && sort.equals(result.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(n, type, groupby, sort);
    }
}
