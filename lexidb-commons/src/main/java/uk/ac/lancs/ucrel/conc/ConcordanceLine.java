package uk.ac.lancs.ucrel.conc;

import uk.ac.lancs.ucrel.Word;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConcordanceLine implements Serializable {

    private List<Word> words = new ArrayList<Word>();

    public void add(Word w){
        words.add(w);
    }

    public List<Word> getWords(){
        return words;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Word w : words){
            sb.append(w.toString()).append(" ");
        }
        return sb.toString();
    }

    public String details(){
        StringBuilder sb = new StringBuilder();
        for(Word w : words){
            sb.append(w.details()).append(" ");
        }
        return sb.toString();
    }

}
