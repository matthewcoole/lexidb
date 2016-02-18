package uk.ac.lancs.ucrel.region;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Matt on 18/02/2016.
 */
public class Data {

    private Path datFilePath;

    public Data(Path dataPath){
        datFilePath = Paths.get(dataPath.toString(), "dat.disco");
    }
}
