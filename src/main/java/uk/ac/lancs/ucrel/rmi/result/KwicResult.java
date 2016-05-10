package uk.ac.lancs.ucrel.rmi.result;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;
import java.util.StringTokenizer;

public class KwicResult extends Result implements Serializable {

    private int context, start, end, total, prePadding, keyPadding;
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
        prePadding = getPrePadding();
        keyPadding = getKeyPadding();
    }

    private int getKeyPadding(){
        int longest = 0;
        for(String s : page){
            StringTokenizer st = new StringTokenizer(s);
            for(int i = 0; i < context; i++){
                st.nextToken();
            }
            String keyword = st.nextToken();
            if(keyword.length() > longest)
                longest = keyword.length();
        }
        return longest;
    }

    private int getPrePadding(){
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
        int toAdd = prePadding - pre.length();
        StringBuilder full = new StringBuilder();
        for(int i = 0; i < toAdd; i++){
            full.append(" ");
        }
        full.append(pre.toString());
        full.append(" ");
        String keyword = st.nextToken();
        toAdd = keyPadding - keyword.length();
        for(int i = 0; i < toAdd/2; i++){
            full.append(" ");
        }
        full.append(keyword);
        for(int i = 0; i < toAdd/2; i++){
            full.append(" ");
        }
        if(toAdd%2!=0)
            full.append(" ");
        full.append("  ");
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
            sb.append("\n(Results " + start + "-" + end + " of " + NumberFormat.getInstance().format(total) + ") Type \"it\" for more.\n");
        System.out.println(sb.toString());
    }
}
