package uk.ac.lancs.ucrel.sort.ngram;

import uk.ac.lancs.ucrel.ngram.NGram;

import java.util.Comparator;

public class FrequencyComparator implements Comparator<NGram> {
    public int compare(NGram n1, NGram n2){
        return n2.getCount() - n1.getCount();
    }
}
