package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistInsertOperationImpl implements InsertOperation {

    private List<InsertOperation> insertOperations = new ArrayList<InsertOperation>();
    private int next = 0;
    private int fileCount = 0;

    public DistInsertOperationImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            insertOperations.add(p.insert());
        }
    }

    @Override
    public boolean sendRaw(String filename, byte[] data) throws RemoteException {
        insertOperations.get(next).sendRaw(filename, data);
        next = (next + 1) % insertOperations.size();
        fileCount++;
        return false;
    }

    @Override
    public void insert() throws RemoteException {
        for (InsertOperation li : insertOperations) {
            li.insert();
        }
    }

    @Override
    public boolean isComplete() throws RemoteException {
        for (InsertOperation li : insertOperations) {
            if (!li.isComplete())
                return false;
        }
        return true;
    }
}
