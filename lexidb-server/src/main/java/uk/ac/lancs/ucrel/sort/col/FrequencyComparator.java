package uk.ac.lancs.ucrel.sort.col;

import uk.ac.lancs.ucrel.col.Collocate;
import java.util.Comparator;

public class FrequencyComparator implements Comparator<Collocate> {
    public int compare(Collocate n1, Collocate n2){
        return n2.getCount() - n1.getCount();
    }
}
