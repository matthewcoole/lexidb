package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistKwicOperationImpl implements KwicOperation {

    private List<KwicOperation> kwicOperations = new ArrayList<KwicOperation>();
    private long time;
    private int next = 0;

    public DistKwicOperationImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            kwicOperations.add(p.kwic());
        }
    }

    public void search(String[] searchTerms, int context, int limit, int sortType, int sortPos, boolean reverseOrder, int pageLength) throws RemoteException {
        long start = System.currentTimeMillis();
        for (KwicOperation k : kwicOperations) {
            k.search(searchTerms, context, limit, sortType, sortPos, reverseOrder, pageLength);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    public int getLength() throws RemoteException {
        int length = 0;
        for (KwicOperation k : kwicOperations) {
            length += k.getLength();
        }
        return length;
    }

    public long getTime() throws RemoteException {
        return time;
    }

    @Override
    public List<Kwic> it() throws RemoteException {
        List<Kwic> r = kwicOperations.get(next).it();
        next = (next + 1) % kwicOperations.size();
        return r;
    }
}
