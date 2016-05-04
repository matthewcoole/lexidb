package uk.ac.lancs.ucrel.rmi;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServerImpl implements Server {

    public boolean shutdown = false;

    public void touch() throws RemoteException {
        System.out.println("Touched!");
    }

    public boolean isShutdown() throws RemoteException {
        return shutdown;
    }

    public void shutdown() throws RemoteException {
        shutdown = true;
    }
}
