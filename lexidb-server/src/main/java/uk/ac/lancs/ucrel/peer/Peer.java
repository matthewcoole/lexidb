package uk.ac.lancs.ucrel.peer;

import uk.ac.lancs.ucrel.ops.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface Peer extends Remote {

    boolean isAvailable() throws RemoteException;

    void notify(String host, int port) throws RemoteException;

    Collection<Peer> getPeers() throws RemoteException;

    InsertOperation insert() throws RemoteException;

    KwicOperation kwic() throws RemoteException;

    NgramOperation ngram() throws RemoteException;

    CollocateOperation collocate() throws RemoteException;

    ListOperation list() throws RemoteException;

}
