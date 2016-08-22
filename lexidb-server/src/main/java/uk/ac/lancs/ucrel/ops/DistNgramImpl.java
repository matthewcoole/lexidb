package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
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
    public void search(String searchTerm, int n) throws RemoteException {

    }

    @Override
    public List<ConcordanceLine> it() throws RemoteException {
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
