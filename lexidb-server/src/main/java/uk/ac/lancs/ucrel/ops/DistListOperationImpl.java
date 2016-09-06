package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.WordListEntry;
import uk.ac.lancs.ucrel.peer.Peer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DistListOperationImpl implements ListOperation {

    private List<ListOperation> lists = new ArrayList<ListOperation>();
    private int next = 0;
    private long time;

    public DistListOperationImpl(Collection<Peer> peers) throws RemoteException {
        for (Peer p : peers) {
            lists.add(p.list());
        }
    }

    @Override
    public void search(String searchTerm, int pageLength, boolean reverseOrder) throws RemoteException {
        long start = System.currentTimeMillis();
        for (ListOperation l : lists) {
            l.search(searchTerm, pageLength, reverseOrder);
        }
        long end = System.currentTimeMillis();
        time = end - start;
    }

    @Override
    public List<WordListEntry> it() throws RemoteException {
        List<WordListEntry> r = lists.get(next).it();
        next = (next + 1) % lists.size();
        return r;
    }

    @Override
    public int getLength() throws RemoteException {
        int length = 0;
        for (ListOperation l : lists) {
            length += l.getLength();
        }
        return length;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
