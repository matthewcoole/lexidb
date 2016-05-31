package uk.ac.lancs.ucrel.node;

import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.peer.PeerImpl;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.server.ServerImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

public class Node {

    private Properties p;
    private Server serverObject;
    private Peer peerObject;

    public Node(Properties p){
        this.p = p;
        peerObject = new PeerImpl(p.getProperty("server.host"),
                Integer.parseInt(p.getProperty("server.port")),
                p.getProperty("server.data.path"),
                p.getProperty("server.peers").split(" "));
        serverObject = new ServerImpl(p.getProperty("server.data.path"), peerObject);
    }

    public void start(){
        int port = Integer.parseInt(p.getProperty("server.port"));
        try {
            Registry r = LocateRegistry.createRegistry(port);

            Server serverStub = (Server) UnicastRemoteObject.exportObject(serverObject, 0);
            r.bind("serv", serverStub);

            Peer peerStub = (Peer)UnicastRemoteObject.exportObject(peerObject, 0);
            r.bind("peer", peerStub);

            ((PeerImpl)peerObject).connectToPeers();

            System.out.println("Waiting for connections on port " + port);

            while(!((ServerImpl)serverObject).isShutdown()) {
                Thread.sleep(3000);
            }

            ((PeerImpl)peerObject).shutdown();

            System.out.println("Shutting down...");

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
