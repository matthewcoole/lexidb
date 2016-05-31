package uk.ac.lancs.ucrel.peer;

import uk.ac.lancs.ucrel.rmi.result.InsertResult;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface Peer extends Remote {

    boolean isAvailable() throws RemoteException;
    void notify(String host, int port) throws RemoteException;
    Collection<Peer> getPeers() throws RemoteException;
    boolean sendRawToInsert(String filename, byte[] data) throws RemoteException;
    InsertResult insertLocal() throws RemoteException;

}
