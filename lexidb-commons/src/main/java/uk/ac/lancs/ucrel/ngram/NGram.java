package uk.ac.lancs.ucrel.ngram;

import uk.ac.lancs.ucrel.Word;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NGram implements Serializable {

    private List<Word> words = new ArrayList<Word>();
    private int count;

    public void add(Word w){
        words.add(w);
    }

    public void setCount(int count){
        this.count = count;
    }

    public List<Word> getWords() {
        return words;
    }

    public int getCount(){
        return count;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Word w : words){
            sb.append(w.toString()).append(" ");
        }
        sb.append("\t").append(count);
        return sb.toString();
    }

}
