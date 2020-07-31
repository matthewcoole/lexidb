package query.json;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Query {
    private Map<String, String> query = new LinkedHashMap<>();
    private Result result = new Result();
}
