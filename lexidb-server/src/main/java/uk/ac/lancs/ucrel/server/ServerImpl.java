package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.ops.*;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.NoSuchObjectException;
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

    private Operation lastOp;

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
        cleanupLastOp();
        lastOp = new DistInsertOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(lastOp, 0);
        return (InsertOperation) lastOp;
    }

    public KwicOperation kwic() throws RemoteException {
        cleanupLastOp();
        lastOp = new DistKwicOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(lastOp, 0);
        return (KwicOperation) lastOp;
    }

    public NgramOperation ngram() throws RemoteException {
        cleanupLastOp();
        lastOp = new DistNgramOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(lastOp, 0);
        return (NgramOperation) lastOp;
    }

    public CollocateOperation collocate() throws RemoteException {
        cleanupLastOp();
        lastOp = new DistCollocateOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(lastOp, 0);
        return (CollocateOperation)lastOp;
    }

    public ListOperation list() throws RemoteException {
        cleanupLastOp();
        lastOp = new DistListOperationImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(lastOp, 0);
        return (ListOperation) lastOp;
    }

    private void cleanupLastOp() throws NoSuchObjectException {
        if(lastOp != null) {
            UnicastRemoteObject.unexportObject(lastOp, true);
        }
    }
}
