package uk.ac.lancs.ucrel.sort;

import java.util.Comparator;

public class ConcLineComparator implements Comparator<int[]> {
    private int pos;
    public ConcLineComparator(int context, int pos){
        this.pos = context + pos;
    }

    public int compare(int[] o1, int[] o2) {
        return o1[pos] - o2[pos];
    }
}
