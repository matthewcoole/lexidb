package storage.conf;

import lombok.Data;
import storage.ColumnSet;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataBlockConf {
    public List<ColumnSet> sets = new ArrayList<>();
    private Path path;
}
