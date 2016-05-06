package uk.ac.lancs.ucrel.rmi.result;

import java.io.Serializable;
import java.util.List;
import java.util.StringTokenizer;

public class KwicResult extends Result implements Serializable {

    private int context, start, end, total, padding;
    private long time;
    private List<String> page;

    public KwicResult(String header, long time, List<String> page, int start, int end, int total, int context){
        super(header);
        this.start = start;
        this.end = end;
        this.total = total;
        this.context = context;
        this.time = time;
        this.page = page;
        padding = getPadding();
    }

    private int getPadding(){
        int longest = 0;
        for(String s : page){
            StringTokenizer st = new StringTokenizer(s);
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < context; i++){
                sb.append(st.nextToken()).append(" ");
            }
            if(sb.length() > longest)
                longest = sb.length();
        }
        return longest;
    }

    private String addPadding(String s){
        StringTokenizer st = new StringTokenizer(s);
        StringBuilder pre = new StringBuilder();
        for(int i = 0; i < context; i++){
            pre.append(st.nextToken()).append(" ");
        }
        int toAdd = padding - pre.length();
        StringBuilder full = new StringBuilder();
        for(int i = 0; i < toAdd; i++){
            full.append(" ");
        }
        full.append(pre.toString());
        full.append(" ").append(st.nextToken()).append("  ");
        while(st.hasMoreTokens()){
            full.append(st.nextToken()).append(" ");
        }
        return full.toString();
    }

    public void print(){
        StringBuilder sb = new StringBuilder("\n");
        if(start == 1)
            sb.append(header).append(" in ").append(time).append("ms.\n\n");
        for(String s : page){
            sb.append(addPadding(s)).append("\n");
        }
        if(end < total)
            sb.append("\n(Results " + start + "-" + end + " of " + total + ") Type \"it\" for more.\n");
        System.out.println(sb.toString());
    }
}
