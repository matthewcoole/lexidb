package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Kwic extends Remote {
    void search(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException;
    Result it() throws RemoteException;
}
