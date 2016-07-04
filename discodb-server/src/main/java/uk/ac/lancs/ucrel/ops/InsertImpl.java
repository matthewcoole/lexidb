package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InsertImpl implements Insert {

    private List<LocalInsert> inserts = new ArrayList<LocalInsert>();
    private int next = 0;
    private int fileCount = 0;

    public InsertImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            inserts.add(p.insert());
        }
    }

    @Override
    public boolean sendRaw(String filename, byte[] data) throws RemoteException {
        inserts.get(next).sendRaw(filename, data);
        next = (next + 1) % inserts.size();
        fileCount++;
        return false;
    }

    @Override
    public void insert() throws RemoteException {
        for(LocalInsert li : inserts){
            li.insert();
        }
    }

    @Override
    public boolean isComplete() throws RemoteException {
        for(LocalInsert li : inserts){
            if(!li.isComplete())
                return false;
        }
        return true;
    }
}
