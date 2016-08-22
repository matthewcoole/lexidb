package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Ngram extends Remote {
    void search(String searchTerm, int n) throws RemoteException;
    List<ConcordanceLine> it() throws RemoteException;
    int getLength() throws RemoteException;
    long getTime() throws RemoteException;
}
