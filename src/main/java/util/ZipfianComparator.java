package util;

import storage.DictionaryEntry;

import java.util.Comparator;

public class ZipfianComparator implements Comparator<DictionaryEntry> {
    public int compare(DictionaryEntry o1, DictionaryEntry o2) {
        for (String key : o1.values.keySet()) {
            int c = o1.values.get(key).toLowerCase().compareTo(o2.values.get(key).toLowerCase());
            if (c == 0)
                c = o1.values.get(key).compareTo(o2.values.get(key));
            if (c != 0)
                return c;
        }
        return 0;
    }
}
