package uk.ac.lancs.ucrel.peer;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ops.*;

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

    private static Logger LOG = Logger.getLogger(PeerImpl.class);

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
        loadDB(dataPath);
        available = true;
    }

    private static void loadDB(String dataPath){
        try {
            LOG.info("Loading database \"" + dataPath.toString() + "\". Please wait...");
            CorpusAccessor.getAccessor(Paths.get(dataPath));
            LOG.info("Database loaded.");
        } catch (Exception e){
            LOG.error(e.getMessage());
        }
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
        return new StringBuilder().append(host).append(":").append(port).toString();
    }

    @Override
    public InsertOperation insert() throws RemoteException {
        InsertOperation li = new LocalInsertOperationImpl(es, Paths.get(dataPath));
        UnicastRemoteObject.exportObject(li, 0);
        return li;
    }

    @Override
    public KwicOperation kwic() throws RemoteException {
        KwicOperation lk = new LocalKwicOperationImpl(Paths.get(dataPath));
        UnicastRemoteObject.exportObject(lk, 0);
        return lk;
    }

    @Override
    public NgramOperation ngram() throws RemoteException {
        NgramOperation ln = new LocalNgramOperationImpl(Paths.get(dataPath));
        UnicastRemoteObject.exportObject(ln, 0);
        return ln;
    }

    @Override
    public CollocateOperation collocate() throws RemoteException {
        CollocateOperation c = new LocalCollocateOperationImpl(Paths.get(dataPath));
        UnicastRemoteObject.exportObject(c, 0);
        return c;
    }

    @Override
    public ListOperation list() throws RemoteException {
        ListOperation l = new LocalListOperationImpl(Paths.get(dataPath));
        UnicastRemoteObject.exportObject(l, 0);
        return l;
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
                LOG.error("Failed to connect to peer " + server + " - " + e.getMessage());
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
                        LOG.info(server + " no longer available.");
                    }
                } catch (Exception e) {
                    connectedPeers.remove(server);
                    LOG.error("Connection to " + server + " lost - " + e.getMessage());
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
            LOG.info("Connected to new peer " + getServerString(host, port));
        }
    }

    @Override
    public boolean isAvailable() throws RemoteException {
        return available;
    }

    @Override
    public void notify(String host, int port) throws RemoteException {
        try {
            LOG.debug("Notification received from " + getServerString(host, port));
            if (!connectedPeers.keySet().contains(getServerString(host, port)))
                connectToPeer(host, port, false);
        } catch (Exception e) {
            LOG.error("Could not connect to peer " + getServerString(host, port) + " - " + e.getMessage());
        }
    }

    public void shutdown() {
        es.shutdown();
    }
}
