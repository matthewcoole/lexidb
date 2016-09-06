package uk.ac.lancs.ucrel.sort.list;

import uk.ac.lancs.ucrel.ds.WordListEntry;

import java.util.Comparator;

public class FrequencyComparator implements Comparator<WordListEntry> {
    public int compare(WordListEntry n1, WordListEntry n2) {
        return n2.getCount() - n1.getCount();
    }
}
