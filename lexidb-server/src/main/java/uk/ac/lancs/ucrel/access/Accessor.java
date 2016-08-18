package uk.ac.lancs.ucrel.access;

import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.index.IndexEntry;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Accessor {

    private Path p;

    public void setPath(Path p){
        this.p = p;
    }

    public Path getPath(){
        return p;
    }

    public void getIndexEntryValues(IndexEntry ie) throws IOException {
        Path indexEntFile = Paths.get(p.toString(), "idx_ent.disco");
        IntBuffer ib = FileUtils.readInts(indexEntFile, ie.getIndexPos(), ie.getCount());
        int[] indexValues = new int[ib.limit()];
        ib.get(indexValues);
        ie.setIndexValues(indexValues);
    }

    public IndexEntry getIndexPos(int numericValue) throws IOException {
        Path indexPosFile = Paths.get(p.toString(), "idx_pos.disco");
        IntBuffer ib = FileUtils.readInts(indexPosFile, numericValue, 2);
        int a = ib.get(0);
        int b = ib.get(1);
        return new IndexEntry(a, b-a);
    }

}
