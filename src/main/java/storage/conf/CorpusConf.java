package storage.conf;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class CorpusConf {
    private String name;
    private int blockSize = 1000000;
    private List<String> dataBlocks = new ArrayList<>();
    private Path path;
}
