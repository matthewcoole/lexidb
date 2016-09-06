package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.ops.*;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerImpl implements Server {

    public boolean shutdown = false;
    private Date startTime;
    private ExecutorService es = Executors.newCachedThreadPool();
    private Peer peerObject;

    public ServerImpl(Peer p) {
        this.startTime = new Date();
        peerObject = p;
    }

    public boolean isShutdown() throws RemoteException {
        return shutdown;
    }

    public void shutdown() throws RemoteException {
        shutdown = true;
        es.shutdown();
    }

    public InsertOperation insert() throws RemoteException {
        InsertOperation i = new DistInsertOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(i, 0);
        return i;
    }

    public KwicOperation kwic() throws RemoteException {
        KwicOperation k = new DistKwicOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(k, 0);
        return k;
    }

    public NgramOperation ngram() throws RemoteException {
        NgramOperation n = new DistNgramOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(n, 0);
        return n;
    }

    public CollocateOperation collocate() throws RemoteException {
        CollocateOperation c = new DistCollocateOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(c, 0);
        return c;
    }

    public ListOperation list() throws RemoteException {
        ListOperation l = new DistListOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(l, 0);
        return l;
    }
}
