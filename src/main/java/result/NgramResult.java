package result;

import util.Pair;

import java.util.ArrayList;
import java.util.List;

public class NgramResult extends Result {
    public List<Pair<String, Integer>> ngrams = new ArrayList<>();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair p : ngrams) {
            sb.append(p.getKey()).append(": ").append(p.getValue()).append('\n');
        }
        return sb.toString().trim();
    }
}
