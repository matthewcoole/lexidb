package uk.ac.lancs.ucrel.sort;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequencyComparator implements Comparator<int[]> {

    private int pos;
    private Map<Integer, Integer> frequencies;

    public FrequencyComparator(int context, int pos, List<int[]> lines){
        this.pos = context + pos;
        frequencies = new HashMap<Integer, Integer>();
        for(int[] line : lines){
            int n = line[this.pos];
            if(!frequencies.containsKey(n))
                frequencies.put(n, 0);
            int count = frequencies.get(n) + 1;
            frequencies.put(n, count);
        }
    }

    public int compare(int[] o1, int[] o2){
        int i = frequencies.get(o2[pos]) - frequencies.get(o1[pos]);
        if(i == 0)
            return o1[pos] - o2[pos];
        return i;
    }
}
