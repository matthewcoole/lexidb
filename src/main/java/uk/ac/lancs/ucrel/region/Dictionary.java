package uk.ac.lancs.ucrel.region;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Matt on 18/02/2016.
 */
public class Dictionary {

    private Path dictFilePath;

    public Dictionary(Path dataPath){
        dictFilePath = Paths.get(dataPath.toString(), "dict.disco");
    }
}
