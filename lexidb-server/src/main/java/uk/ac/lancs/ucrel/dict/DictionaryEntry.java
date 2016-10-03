package uk.ac.lancs.ucrel.dict;

import java.util.ArrayList;
import java.util.List;

public class DictionaryEntry {
    private String word;
    private List<String> tags = new ArrayList<String>();
    private int value;
    private int count;

    public DictionaryEntry(String tsv, int value) {
        setVals(tsv, value);
    }

    protected DictionaryEntry(String tsv, int value, int count) {
        setVals(tsv, value);
        this.count = count;
    }

    public static String cleanTsv(String tsv) {
        StringBuilder sb = new StringBuilder();
        String[] tsvs = tsv.split("\t");
        sb.append(tsvs[0].trim());
        for (int i = 1; i < tsvs.length; i++) {
            sb.append('\t').append(tsvs[i]);
        }
        return sb.toString();
    }

    private void setVals(String tsv, int value) {
        tsv = cleanTsv(tsv);
        String[] tsvs = tsv.split("\t");
        this.word = tsvs[0];
        for (int i = 1; i < tsvs.length; i++) {
            tags.add(tsvs[i]);
        }
        this.value = value;
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
        if (o instanceof DictionaryEntry) {
            DictionaryEntry de = (DictionaryEntry) o;
            if (de.getWord().equals(word)) {
                if (de.getTags().size() == tags.size())
                    for (int i = 0; i < tags.size(); i++) {
                        if (!de.getTags().get(i).equals(tags.get(i)))
                            return false;
                    }
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word);
        for (String tag : tags) {
            sb.append('\t').append(tag);
        }
        return sb.toString();
    }
}
