package uk.ac.lancs.ucrel.region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Index {
    private Path regionPath, idxPath;

    public Index(Path regionPath) throws IOException {
        this.regionPath = regionPath;
        idxPath = Paths.get(regionPath.toString(), "idx.disco");
        Files.deleteIfExists(idxPath);
        Files.createFile(idxPath);
    }

    public void add(int val, int pos){

    }

    public void save(){

    }
}
