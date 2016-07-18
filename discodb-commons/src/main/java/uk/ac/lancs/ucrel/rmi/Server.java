package uk.ac.lancs.ucrel.rmi;

import uk.ac.lancs.ucrel.ops.Insert;
import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.InsertResultImpl;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    void shutdown() throws RemoteException;
    Insert insert() throws RemoteException;
    Result kwic(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int page) throws RemoteException;
    Result list(String searchTerm) throws RemoteException;
    Result it() throws RemoteException;
    Result status() throws RemoteException;

}
