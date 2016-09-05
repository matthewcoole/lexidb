package uk.ac.lancs.ucrel.sort.kwic;

import java.util.Comparator;

public class LexicalComparator implements Comparator<int[]> {
    private int pos;

    public LexicalComparator(int context, int pos) {
        this.pos = context + pos;
    }

    public int compare(int[] o1, int[] o2) {
        return o1[pos] - o2[pos];
    }
}
