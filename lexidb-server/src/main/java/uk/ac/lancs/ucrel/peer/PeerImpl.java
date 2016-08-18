package uk.ac.lancs.ucrel.peer;

import uk.ac.lancs.ucrel.ops.Insert;
import uk.ac.lancs.ucrel.ops.Kwic;
import uk.ac.lancs.ucrel.ops.LocalInsertImpl;
import uk.ac.lancs.ucrel.ops.LocalKwicImpl;

import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerImpl implements Peer {

    private String host, dataPath;
    private int port;
    private String[] peers;
    private Map<String, Peer> connectedPeers;
    private boolean available;
    private ExecutorService es = Executors.newCachedThreadPool();

    public PeerImpl(String host, int port, String dataPath, String... peers) {
        this.host = host;
        this.port = port;
        this.dataPath = dataPath;
        this.peers = peers;
        available = true;
    }

    @Override
    public Insert insert() throws RemoteException {
        Insert li = new LocalInsertImpl(es, Paths.get(dataPath));
        UnicastRemoteObject.exportObject(li, 0);
        return li;
    }

    @Override
    public Kwic kwic() throws RemoteException {
        Kwic lk = new LocalKwicImpl(es, Paths.get(dataPath));
        UnicastRemoteObject.exportObject(lk, 0);
        return lk;
    }

    public Collection<Peer> getPeers() {
        return connectedPeers.values();
    }

    public void connectToPeers() {
        es.execute(() -> connect());
    }

    private void connect() {
        connectedPeers = new HashMap<String, Peer>();
        while (!available) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        for (String server : peers) {
            try {
                connectToPeer(getHost(server), getPort(server), true);
            } catch (Exception e) {
                System.out.println("Could not connect to peer " + server);
            }
        }
        while (available) {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }
            for (String server : connectedPeers.keySet()) {
                Peer s = connectedPeers.get(server);
                try {
                    if (!s.isAvailable()) {
                        connectedPeers.remove(server);
                        System.out.println(server + " no longer avaialbe.");
                    }
                } catch (Exception e) {
                    connectedPeers.remove(server);
                    System.out.println("Connection to " + server + " lost.");
                }
            }
        }
    }

    private void connectToPeer(String host, int port, boolean notify) throws RemoteException, NotBoundException {
        Registry r = LocateRegistry.getRegistry(host, port);
        Remote tmp = r.lookup("peer");
        Peer p = null;
        if (tmp instanceof Peer)
            p = (Peer) tmp;
        if (p != null) {
            connectedPeers.put(getServerString(host, port), p);
            if (notify)
                p.notify(host, port);
            System.out.println("Connected to new peer: " + getServerString(host, port));
        }
    }

    @Override
    public boolean isAvailable() throws RemoteException {
        return available;
    }

    @Override
    public void notify(String host, int port) throws RemoteException {
        try {
            System.out.println("Notification received: " + getServerString(host, port));
            if (!connectedPeers.keySet().contains(getServerString(host, port)))
                connectToPeer(host, port, false);
        } catch (Exception e) {
            System.err.println("Could not connect to peer " + getServerString(host, port) + ": " + e.getMessage());
        }
    }

    public void shutdown() {
        es.shutdown();
    }

    private static String getHost(String serverString) {
        return split(serverString)[0];
    }

    private static int getPort(String serverString) {
        return Integer.parseInt(split(serverString)[1]);
    }

    private static String[] split(String serverString) {
        return serverString.split(":");
    }

    private static String getServerString(String host, int port) {
        return new StringBuilder().append(host).append(port).toString();
    }
}
