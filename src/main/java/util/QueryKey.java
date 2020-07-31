package util;

import lombok.AllArgsConstructor;
import lombok.Data;
import query.json.Query;

import java.nio.file.Path;

@AllArgsConstructor
@Data
public class QueryKey {
    private Path dataPath;
    private Query query;
}
