package uk.ac.lancs.ucrel.rmi.result;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InsertResult extends Remote {
    String status() throws RemoteException;
    boolean isComplete() throws RemoteException;
}
