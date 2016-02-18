package uk.ac.lancs.ucrel.region;

import java.nio.file.Path;

/**
 * Created by Matt on 18/02/2016.
 */
public class Region {

    private static int MAX_SIZE = 1000000;

    private Data dat;
    private Dictionary dict;
    private Index idx;

    public Region(Path dataPath){
        dat = new Data(dataPath);
        dict = new Dictionary(dataPath);
        idx = new Index(dataPath);
    }
}
