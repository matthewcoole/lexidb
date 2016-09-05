package uk.ac.lancs.ucrel.sort.ngram;

import uk.ac.lancs.ucrel.ds.Ngram;

import java.util.Comparator;

public class FrequencyComparator implements Comparator<Ngram> {
    public int compare(Ngram n1, Ngram n2) {
        return n2.getCount() - n1.getCount();
    }
}
