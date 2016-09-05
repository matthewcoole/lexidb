package uk.ac.lancs.ucrel.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Kwic implements Serializable {

    private List<Word> words = new ArrayList<Word>();

    public void add(Word w) {
        words.add(w);
    }

    public List<Word> getWords() {
        return words;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Word w : words) {
            sb.append(w.toString()).append(" ");
        }
        return sb.toString();
    }
}
