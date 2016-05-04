package uk.ac.lancs.ucrel.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    void touch() throws RemoteException;
    boolean isShutdown() throws RemoteException;
    void shutdown() throws RemoteException;
}
