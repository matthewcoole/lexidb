package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistKwicImpl implements Kwic {

    private List<Kwic> kwics = new ArrayList<Kwic>();
    private int next = 0;

    public DistKwicImpl(Collection<Peer> peers) throws RemoteException {
        for(Peer p : peers){
            kwics.add(p.kwic());
        }
    }

    public void search(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException {
        for(Kwic k : kwics){
            k.search(searchTerm, context, limit, sortType, sortPos, order, pageLength);
        }
    }

    @Override
    public Result it() throws RemoteException {
        Result r = kwics.get(next).it();
        next = (next + 1) % kwics.size();
        return r;
    }
}
