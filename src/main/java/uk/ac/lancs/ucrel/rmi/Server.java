package uk.ac.lancs.ucrel.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    boolean isShutdown() throws RemoteException;
    void shutdown() throws RemoteException;
    Result insert(String path) throws RemoteException;
    Result search(String keyword) throws RemoteException;
    Result search(String keyword, int sort) throws RemoteException;
}
