package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistKwicImpl implements Kwic {

    private List<Kwic> kwics = new ArrayList<Kwic>();
    private long time;
    private int next = 0;

    public DistKwicImpl(Collection<Peer> peers) throws RemoteException {
        for(Peer p : peers){
            kwics.add(p.kwic());
        }
    }

    public void search(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException {
        long start = System.currentTimeMillis();
        for(Kwic k : kwics){
            k.search(searchTerm, context, limit, sortType, sortPos, order, pageLength);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    public int getLength() throws RemoteException {
        int length = 0;
        for(Kwic k : kwics){
            length += k.getLength();
        }
        return length;
    }

    public long getTime() throws RemoteException {
        return time;
    }

    @Override
    public List<ConcordanceLine> it() throws RemoteException {
        List<ConcordanceLine> r = kwics.get(next).it();
        next = (next + 1) % kwics.size();
        return r;
    }
}
