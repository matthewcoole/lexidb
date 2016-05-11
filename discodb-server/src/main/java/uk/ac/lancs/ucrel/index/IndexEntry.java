package uk.ac.lancs.ucrel.index;

import java.util.ArrayList;
import java.util.List;

public class IndexEntry {

    private int indexPos;
    private int count;
    private int[] indexValues;
    private List<Integer> indexValuesAsList;

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

    public List<Integer> getIndexValuesAsList(){
        return indexValuesAsList;
    }

    public void setIndexValues(int[] indexValues) {
        this.indexValues = indexValues;
        indexValuesAsList = new ArrayList<Integer>();
        for(int i : indexValues)
            indexValuesAsList.add(i);
    }
}
