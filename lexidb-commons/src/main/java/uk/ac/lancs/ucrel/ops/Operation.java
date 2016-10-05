package uk.ac.lancs.ucrel.ops;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Operation extends Remote {

    boolean isComplete() throws RemoteException;

    long getTime() throws RemoteException;

}
