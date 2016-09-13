package uk.ac.lancs.ucrel.node;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.peer.PeerImpl;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.server.ServerImpl;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;

public class Node {

    private static Logger LOG = Logger.getLogger(Node.class);
    private Properties p;
    private Server serverObject;
    private Peer peerObject;

    public Node(Properties p) {
        this.p = p;
        peerObject = new PeerImpl(p.getProperty("node.host"),
                Integer.parseInt(p.getProperty("node.port")),
                p.getProperty("node.data.path"),
                p.getProperty("node.peers").split(" "));
        serverObject = new ServerImpl(peerObject);
    }

    public void start() {
        int port = Integer.parseInt(p.getProperty("node.port"));
        try {
            Registry r = LocateRegistry.createRegistry(port);

            Server serverStub = (Server) UnicastRemoteObject.exportObject(serverObject, 0);
            r.bind("serv", serverStub);

            Peer peerStub = (Peer) UnicastRemoteObject.exportObject(peerObject, 0);
            r.bind("peer", peerStub);

            ((PeerImpl) peerObject).connectToPeers();

            LOG.info("Listening for peers on port " + port);

            while (!((ServerImpl) serverObject).isShutdown()) {
                Thread.sleep(3000);
            }

            ((PeerImpl) peerObject).shutdown();

            LOG.info("Shuting down...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
