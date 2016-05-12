package uk.ac.lancs.ucrel.rmi.result;

public class InsertResult extends Result {

    private long time;
    private boolean complete;

    public InsertResult(String msg, boolean complete){
        super(msg);
        this.complete = complete;
    }

    public boolean isComplete(){
        return complete;
    }

    public void print(){
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        if(complete)
            System.out.println(sb.toString());
        else
            System.out.print(sb.toString());
    }
}
