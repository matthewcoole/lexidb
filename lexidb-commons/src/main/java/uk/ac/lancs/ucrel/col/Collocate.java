package uk.ac.lancs.ucrel.col;

import uk.ac.lancs.ucrel.Word;

import java.io.Serializable;

public class Collocate implements Serializable {
    private Word collocate;
    private String searchTerm;
    int count;

    public Collocate(Word collocate, String searchTerm, int count){
        this.collocate = collocate;
        this.searchTerm = searchTerm;
        this.count = count;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(collocate.toString()).append("\t").append(count);
        return sb.toString();
    }

    public void increment(){
        count++;
    }

    public Word getWord(){
        return collocate;
    }

    public int getCount(){
        return count;
    }
}
