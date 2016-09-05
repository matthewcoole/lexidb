package uk.ac.lancs.ucrel.ds;

import java.io.Serializable;

public class Collocate implements Serializable {
    int count;
    private Word collocate;

    public Collocate(Word collocate, int count) {
        this.collocate = collocate;
        this.count = count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(collocate.toString()).append("\t").append(count);
        return sb.toString();
    }

    public void increment() {
        count++;
    }

    public Word getWord() {
        return collocate;
    }

    public int getCount() {
        return count;
    }
}
