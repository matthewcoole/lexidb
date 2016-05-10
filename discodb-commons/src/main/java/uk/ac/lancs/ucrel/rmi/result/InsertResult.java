package uk.ac.lancs.ucrel.rmi.result;

public class InsertResult extends Result {

    private long time;

    public InsertResult(String header, long time){
        super(header);
        this.time = time;
    }

    public void print(){
        StringBuilder sb = new StringBuilder("\n");
        sb.append(header).append(" in ").append(time).append("ms.").append("\n");
        System.out.println(sb.toString());
    }
}
