package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.ngram.NGram;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Ngram extends Remote {
    void search(String searchTerm, int n, int pos, int pageLength) throws RemoteException;
    List<NGram> it() throws RemoteException;
    int getLength() throws RemoteException;
    long getTime() throws RemoteException;
}
