package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ops.DistKwicImpl;
import uk.ac.lancs.ucrel.ops.Insert;
import uk.ac.lancs.ucrel.ops.DistInsertImpl;
import uk.ac.lancs.ucrel.ops.Kwic;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.rmi.result.Result;
import uk.ac.lancs.ucrel.rmi.Server;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerImpl implements Server {

    public boolean shutdown = false;
    private Date startTime;
    private CorpusAccessor ca;
    private ExecutorService es = Executors.newCachedThreadPool();
    private Peer peerObject;

    public ServerImpl(Peer p){
        this.startTime = new Date();
        peerObject = p;
    }

    public Result status() throws RemoteException {
        StringBuilder sb = new StringBuilder();
        sb.append("Server status:\n\n");
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String name = "unknown";
        try {
            name = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {

        }
        sb.append("\tstarted:\t").append(df.format(startTime)).append("\n");
        sb.append("\thostname:\t").append(name).append("\n");
        String wordCount = NumberFormat.getInstance().format(ca.getWordCount());
        sb.append("\twords:\t\t").append(wordCount).append("\n");
        String typeCount = NumberFormat.getInstance().format(ca.getWordTypeCount());
        sb.append("\ttypes:\t\t").append(typeCount);
        return new Result(sb.toString());
    }

    public boolean isShutdown() throws RemoteException {
        return shutdown;
    }

    public void shutdown() throws RemoteException {
        shutdown = true;
        es.shutdown();
    }

    public Insert insert() throws RemoteException {
        Insert i = new DistInsertImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(i, 0);
        return i;
    }

    public Kwic kwic() throws RemoteException {
        Kwic k = new DistKwicImpl(peerObject.getPeers());
        UnicastRemoteObject.exportObject(k, 0);
        return k;
    }

    public Result list(String searchTerm) throws RemoteException {
        return null;
    }
}
