package uk.ac.lancs.ucrel.rmi.result;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class InsertResultImpl extends Result implements InsertResult {

    private long time;
    private boolean complete;
    private List<InsertResult> insertions;

    public InsertResultImpl(String msg){
        super(msg);
        complete = false;
        insertions = new ArrayList<InsertResult>();
    }

    public void addInsertion(InsertResult r){
        insertions.add(r);
    }

    public void setComplete(boolean complete){
        this.complete = complete;
    }

    public boolean isComplete(){
        if(insertions.size() == 0)
            return complete;
        else {
            for(InsertResult r : insertions){
                try {
                    if(!r.isComplete())
                        return false;
                } catch (RemoteException e) {
                    System.err.println(e.getMessage());
                }
            }
            return true;
        }
    }

    public String status(){
        StringBuilder sb = new StringBuilder();
        sb.append(header);
        if(complete)
            System.out.println(sb.toString());
        else
            System.out.print(sb.toString());
        return sb.toString();
    }
}
