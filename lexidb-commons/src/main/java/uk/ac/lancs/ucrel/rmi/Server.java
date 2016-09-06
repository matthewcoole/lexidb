package uk.ac.lancs.ucrel.rmi;

import uk.ac.lancs.ucrel.ops.*;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    void shutdown() throws RemoteException;

    InsertOperation insert() throws RemoteException;

    KwicOperation kwic() throws RemoteException;

    NgramOperation ngram() throws RemoteException;

    CollocateOperation collocate() throws RemoteException;

    ListOperation list() throws RemoteException;
}
