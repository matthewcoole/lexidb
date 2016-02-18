package uk.ac.lancs.ucrel.region;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Matt on 18/02/2016.
 */
public class Index {
    private Path idxFilePath;

    public Index(Path dataPath){
        idxFilePath = Paths.get(dataPath.toString(), "idx.disco");
    }
}
