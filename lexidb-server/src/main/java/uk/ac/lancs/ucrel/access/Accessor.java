package uk.ac.lancs.ucrel.access;

import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.index.IndexEntry;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class Accessor {

    protected Path p;

    public Path getPath() {
        return p;
    }

    public void setPath(Path p) {
        this.p = p;
    }

    public void getIndexEntryValues(Collection<IndexEntry> ies) throws IOException {
        int first = Integer.MAX_VALUE;
        int last = 0;
        for (IndexEntry ie : ies) {
            int pos = ie.getIndexPos();
            int size = ie.getCount();
            first = (pos < first) ? pos : first;
            last = (pos + size > last) ? pos + size : last;
        }
        Path indexEntFile = Paths.get(p.toString(), "idx_ent.disco");
        IntBuffer ib = FileUtils.readInts(indexEntFile, first, last - first);
        for (IndexEntry ie : ies) {
            int[] indexValues = new int[ie.getCount()];
            ib.position(ie.getIndexPos() - first);
            ib.get(indexValues, 0, ie.getCount());
            ie.setIndexValues(indexValues);
        }
    }

    public Map<Integer, IndexEntry> getIndexPos(Collection<Integer> numericValues) throws IOException {
        int first = Integer.MAX_VALUE;
        int last = 0;
        for (int i : numericValues) {
            first = (i < first) ? i : first;
            last = (i + 2 > last) ? i + 2 : last;
        }
        Path indexPosFile = Paths.get(p.toString(), "idx_pos.disco");
        IntBuffer ib = FileUtils.readInts(indexPosFile, first, last - first);
        Map<Integer, IndexEntry> ies = new HashMap<Integer, IndexEntry>();
        for (int i : numericValues) {
            int[] entry = new int[2];
            ib.position(i - first);
            ib.get(entry, 0, 2);
            ies.put(i, new IndexEntry(entry[0], entry[1] - entry[0]));
        }
        return ies;
    }
}
