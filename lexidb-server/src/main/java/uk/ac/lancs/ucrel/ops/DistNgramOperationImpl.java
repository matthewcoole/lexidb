package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.Ngram;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistNgramOperationImpl implements NgramOperation {

    private List<NgramOperation> ngramOperations = new ArrayList<NgramOperation>();
    private int next = 0;
    private long time;

    public DistNgramOperationImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            ngramOperations.add(p.ngram());
        }
    }

    @Override
    public void search(String searchTerm, int n, int pos, int pageLength, boolean reverseOrder) throws RemoteException {
        long start = System.currentTimeMillis();
        for (NgramOperation ng : ngramOperations) {
            ng.search(searchTerm, n, pos, pageLength, reverseOrder);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    @Override
    public List<Ngram> it() throws RemoteException {
        List<Ngram> r = ngramOperations.get(next).it();
        next = (next + 1) % ngramOperations.size();
        return r;
    }

    @Override
    public int getLength() throws RemoteException {
        int length = 0;
        for (NgramOperation ng : ngramOperations) {
            length += ng.getLength();
        }
        return length;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
