package uk.ac.lancs.ucrel.ops;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Collocate extends Remote {
    void search(String searchTerm, int contextLeft, int contextRight, int pageLength) throws RemoteException;
    List<uk.ac.lancs.ucrel.col.Collocate> it() throws RemoteException;
    int getLength() throws RemoteException;
    long getTime() throws RemoteException;
}
