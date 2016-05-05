package uk.ac.lancs.ucrel.rmi;

import java.io.Serializable;
import java.util.List;

public class Result implements Serializable {

    String header;
    long time;
    List<String> results;

    public Result(String header, long time, List<String> results){
        this.header = header;
        this.time = time;
        this.results = results;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder("\n");
        sb.append(header).append(" in ").append(time).append("ms.\n\n");
        for(String s : results){
            sb.append(s).append("\n");
        }
        return sb.toString();
    }
}
