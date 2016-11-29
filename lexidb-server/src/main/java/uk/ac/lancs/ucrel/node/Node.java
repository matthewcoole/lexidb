package uk.ac.lancs.ucrel.node;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
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
                p.getProperty("node.tmp.path"),
                p.getProperty("node.peers").split(" "));
        serverObject = new ServerImpl(peerObject);
    }

    public void startJetty(){

        LOG.info("Starting jetty server...");

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        org.eclipse.jetty.server.Server jettyServer = new org.eclipse.jetty.server.Server(Integer.parseInt(p.getProperty("server.port")));
        jettyServer.setHandler(context);
        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "uk.ac.lancs.ucrel.handler");

        try {
            jettyServer.start();
            jettyServer.join();
        } catch(Exception e) {
            LOG.error(e.getMessage());
        } finally{
            jettyServer.destroy();
        }
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

            startJetty();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
