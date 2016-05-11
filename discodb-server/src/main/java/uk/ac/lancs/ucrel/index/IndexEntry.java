package uk.ac.lancs.ucrel.index;

public class IndexEntry {

    private int indexPos;
    private int count;
    private int[] indexValues;

    public IndexEntry(int indexPos, int count){
        this.indexPos = indexPos;
        this.count = count;
    }

    public int getIndexPos() {
        return indexPos;
    }

    public int getCount() {
        return count;
    }

    public int[] getIndexValues() {
        return indexValues;
    }

    public void setIndexValues(int[] indexValues) {
        this.indexValues = indexValues;
    }
}
