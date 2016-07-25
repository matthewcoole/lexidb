package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Kwic extends Remote {
    void search(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException;
    List<ConcordanceLine> it() throws RemoteException;
    int getLength() throws RemoteException;
    long getTime() throws RemoteException;
}
