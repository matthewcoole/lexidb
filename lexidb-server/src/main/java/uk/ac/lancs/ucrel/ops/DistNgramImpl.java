package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ngram.NGram;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistNgramImpl implements Ngram {

    List<Ngram> ngrams = new ArrayList<Ngram>();

    public DistNgramImpl(Collection<Peer> peers) throws RemoteException {
        for(Peer p : peers){
            ngrams.add(p.ngram());
        }
    }

    @Override
    public void search(String searchTerm, int n, int pos, int pageLength) throws RemoteException {

    }

    @Override
    public List<NGram> it() throws RemoteException {
        return null;
    }

    @Override
    public int getLength() throws RemoteException {
        return 0;
    }

    @Override
    public long getTime() throws RemoteException {
        return 0;
    }
}
