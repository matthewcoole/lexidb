package uk.ac.lancs.ucrel.ds;

import java.io.Serializable;

public class Collocate implements Serializable {
    int[] count;
    int pos;
    private Word collocate;

    public Collocate(Word collocate, int size, int pos) {
        this.collocate = collocate;
        count = new int[size];
        this.pos = pos;
        count[pos] = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(collocate.toString()).append('\t').append(getAllCounts());
        return sb.toString();
    }

    public void increment(int i) {
        count[i]++;
    }

    public Word getWord() {
        return collocate;
    }

    public String getAllCounts() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count.length; i++) {
            if (i != pos)
                sb.append(count[i]);
            else
                sb.append('#');
            sb.append('\t');
        }
        sb.append('(').append(getCount()).append(')');
        return sb.toString();
    }

    public int getCount(int i) {
        return count[i];
    }

    public int getCount() {
        int total = 0;
        for (int i : count) {
            total += i;
        }
        return total;
    }
}
