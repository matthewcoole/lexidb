package storage;

import java.util.Map;

public class DictionaryEntry {
    public Map<String, String> values;
    public int count;

    public DictionaryEntry(Map<String, String> vals) {
        values = vals;
    }

    public String toString() {
        return values.toString();
    }
}
