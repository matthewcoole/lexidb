package uk.ac.lancs.ucrel.peer;

import uk.ac.lancs.ucrel.ops.Insert;
import uk.ac.lancs.ucrel.ops.Kwic;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Collection;

public interface Peer extends Remote {

    boolean isAvailable() throws RemoteException;
    void notify(String host, int port) throws RemoteException;
    Collection<Peer> getPeers() throws RemoteException;
    Insert insert() throws RemoteException;
    Kwic kwic() throws RemoteException;

}