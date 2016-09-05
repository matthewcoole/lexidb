package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.ds.Ngram;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NgramOperation extends Remote {
    void search(String searchTerm, int n, int pos, int pageLength, boolean reverseOrder) throws RemoteException;

    List<Ngram> it() throws RemoteException;

    int getLength() throws RemoteException;

    long getTime() throws RemoteException;
}
