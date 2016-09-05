package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistCollocationImpl implements Collocate {

    private List<Collocate> cols = new ArrayList<Collocate>();
    private int next = 0;
    private long time;

    public DistCollocationImpl(Collection<Peer> peers) throws RemoteException {
        for(Peer p : peers){
            cols.add(p.collocate());
        }
    }

    @Override
    public void search(String searchTerm, int contextLeft, int contextRight, int pageLength) throws RemoteException {
        long start = System.currentTimeMillis();
        for(Collocate ng : cols){
            ng.search(searchTerm, contextLeft, contextRight, pageLength);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    @Override
    public List<uk.ac.lancs.ucrel.col.Collocate> it() throws RemoteException {
        List<uk.ac.lancs.ucrel.col.Collocate> r = cols.get(next).it();
        next = (next + 1) % cols.size();
        return r;
    }

    @Override
    public int getLength() throws RemoteException {
        int length = 0;
        for(Collocate c : cols){
            length += c.getLength();
        }
        return length;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
