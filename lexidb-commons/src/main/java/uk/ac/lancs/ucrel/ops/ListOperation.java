package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.WordListEntry;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ListOperation extends Operation {
    void search(String[] searchTerms, int pageLength, boolean reverseOrder) throws RemoteException;

    List<WordListEntry> it() throws RemoteException;

    int getLength() throws RemoteException;

    long getTime() throws RemoteException;
}
