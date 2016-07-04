package uk.ac.lancs.ucrel.ops;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Insert extends Remote {

    boolean sendRaw(String filename, byte[] data) throws RemoteException;
    void insert() throws RemoteException;
    boolean isComplete() throws RemoteException;

}
