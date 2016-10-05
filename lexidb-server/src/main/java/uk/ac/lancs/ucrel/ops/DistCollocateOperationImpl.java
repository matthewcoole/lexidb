package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistCollocateOperationImpl implements CollocateOperation {

    private List<CollocateOperation> cols = new ArrayList<CollocateOperation>();
    private int next = 0;
    private long start, end;

    public DistCollocateOperationImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            cols.add(p.collocate());
        }
    }

    @Override
    public void search(String[] searchTerms, int contextLeft, int contextRight, int pageLength, boolean reverseOrder) throws RemoteException {
        start = System.currentTimeMillis();
        for (CollocateOperation c : cols) {
            c.search(searchTerms, contextLeft, contextRight, pageLength, reverseOrder);
        }
        while(!isComplete()){
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<Collocate> it() throws RemoteException {
        List<Collocate> r = cols.get(next).it();
        next = (next + 1) % cols.size();
        return r;
    }

    @Override
    public int getLength() throws RemoteException {
        int length = 0;
        for (CollocateOperation c : cols) {
            length += c.getLength();
        }
        return length;
    }

    @Override
    public boolean isComplete() throws RemoteException {
        for(CollocateOperation c : cols){
            if(!c.isComplete())
                return false;
        }
        end = System.currentTimeMillis();
        return true;
    }

    @Override
    public long getTime() throws RemoteException {
        return end - start;
    }
}
