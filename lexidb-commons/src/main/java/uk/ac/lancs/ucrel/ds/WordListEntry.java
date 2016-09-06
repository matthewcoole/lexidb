package uk.ac.lancs.ucrel.ds;

import java.io.Serializable;

public class WordListEntry implements Serializable {
    private Word word;
    private int count;

    public WordListEntry(Word w, int count) {
        this.word = w;
        this.count = count;
    }

    public Word getWord() {
        return word;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(word.toString()).append('\t').append(count);
        return sb.toString();
    }
}
