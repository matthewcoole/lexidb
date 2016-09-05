package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ngram.NGram;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistNgramImpl implements Ngram {

    private List<Ngram> ngrams = new ArrayList<Ngram>();
    private int next = 0;
    private long time;

    public DistNgramImpl(Collection<Peer> peers) throws RemoteException {
        for(Peer p : peers){
            ngrams.add(p.ngram());
        }
    }

    @Override
    public void search(String searchTerm, int n, int pos, int pageLength) throws RemoteException {
        long start = System.currentTimeMillis();
        for(Ngram ng : ngrams){
            ng.search(searchTerm, n, pos, pageLength);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    @Override
    public List<NGram> it() throws RemoteException {
        List<NGram> r = ngrams.get(next).it();
        next = (next + 1) % ngrams.size();
        return r;
    }

    @Override
    public int getLength() throws RemoteException {
        int length = 0;
        for(Ngram ng : ngrams){
            length += ng.getLength();
        }
        return length;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
