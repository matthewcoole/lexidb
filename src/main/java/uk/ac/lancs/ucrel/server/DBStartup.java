package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.server.ServerImpl;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DBStartup {

    private static final int PORT = 1289;

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {
        Server s = new ServerImpl();
        Registry r = LocateRegistry.createRegistry(PORT);
        Server stub = (Server) UnicastRemoteObject.exportObject(s, 0);
        r.bind("serv", stub);
        System.out.println("Waiting for connections on port " + PORT);
        while(!s.isShutdown()) {
            Thread.sleep(3000);
        }
        System.out.println("Shutting down...");
        System.exit(0);
    }
}
