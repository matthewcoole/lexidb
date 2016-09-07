package uk.ac.lancs.ucrel.dict;

import java.util.ArrayList;
import java.util.List;

public class DictionaryEntry {
    private String word;
    private List<String> tags = new ArrayList<String>();
    private int value;
    private int count;

    public DictionaryEntry(String tsv, int value) {
        String[] tsvs = tsv.split("\t");
        this.word = tsvs[0].trim();
        for(int i = 1; i < tsvs.length; i++){
            tags.add(tsvs[i].trim());
        }
        this.value = value;
    }

    protected DictionaryEntry(String tsv, int value, int count) {
        String[] tsvs = tsv.split("\t");
        this.word = tsvs[0].trim();
        for(int i = 1; i < tsvs.length; i++){
            tags.add(tsvs[i].trim());
        }
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(word);
        for(String tag : tags){
            sb.append('\t').append(tag);
        }
        return sb.toString();
    }
}
