package uk.ac.lancs.ucrel.dict;

import java.util.ArrayList;
import java.util.List;

public class DictionaryEntry {
    private String word;
    private List<String> tags = new ArrayList<String>();
    private int value;
    private int count;

    public DictionaryEntry(String word, int value) {
        this.word = word;
        this.value = value;
    }

    protected DictionaryEntry(String word, int value, int count) {
        this.word = word;
        this.value = value;
        this.count = count;
    }

    public String getWord() {
        return word;
    }

    public int getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    public void increment() {
        count++;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void addTags(List<String> tags) {
        this.tags.addAll(tags);
    }

    public List<String> getTags() {
        return tags;
    }

    public void addToCount(int n) {
        count += n;
    }

    public boolean equals(Object o) {
        if (o instanceof DictionaryEntry)
            return ((DictionaryEntry) o).getWord().equals(word);
        return false;
    }
}
