package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.rmi.Server;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

public class DBStartup {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {
        Properties  p = loadServerProperties();
        Server s = new ServerImpl(p);
        int port = Integer.parseInt(p.getProperty("server.port"));
        Registry r = LocateRegistry.createRegistry(port);
        Server stub = (Server) UnicastRemoteObject.exportObject(s, 0);
        r.bind("serv", stub);
        ((ServerImpl)s).setAvailable(true);
        System.out.println("Waiting for connections on port " + port);
        while(!s.isShutdown()) {
            Thread.sleep(3000);
        }
        System.out.println("Shutting down...");
        System.exit(0);
    }

    private static Properties loadServerProperties(){
        Properties p = new Properties();
        InputStream is = DBStartup.class.getClassLoader().getResourceAsStream("server.properties");
        try {
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }
}
