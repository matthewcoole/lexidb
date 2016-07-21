package uk.ac.lancs.ucrel.rmi;

import uk.ac.lancs.ucrel.ops.Insert;
import uk.ac.lancs.ucrel.ops.Kwic;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    void shutdown() throws RemoteException;
    Insert insert() throws RemoteException;
    Kwic kwic() throws RemoteException;
    Result list(String searchTerm) throws RemoteException;
    Result status() throws RemoteException;

}
