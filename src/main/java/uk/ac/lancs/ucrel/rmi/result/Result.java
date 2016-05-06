package uk.ac.lancs.ucrel.rmi.result;

import java.io.Serializable;

public class Result implements Serializable {

    String header;


    public Result(String header){
        this.header = header;
    }

    public void print(){
        StringBuilder sb = new StringBuilder("\n");
        sb.append(header).append("\n");
        System.out.println(sb.toString());
    }
}
