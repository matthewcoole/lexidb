package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.parser.TextParser;
import uk.ac.lancs.ucrel.peer.Peer;
import uk.ac.lancs.ucrel.result.FullKwicResult;
import uk.ac.lancs.ucrel.result.FullResult;
import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.Result;
import uk.ac.lancs.ucrel.rmi.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerImpl implements Server {

    public boolean shutdown = false;
    private Date startTime;
    private String dataPath;
    private CorpusAccessor ca;
    private int nextPeer;
    private ExecutorService es = Executors.newCachedThreadPool();
    private InsertResult lastInsert;
    private Path rawTempPath;
    //private Path rawToInsert;
    //private Map<String, Server> peers;
    private Peer peerObject;

    private FullResult lastResult;

    public ServerImpl(String dataPath, Peer p){
        this.startTime = new Date();
        this.dataPath = dataPath;
        peerObject = p;
        //peers = new HashMap<String, Server>();
        try {
            ca = new CorpusAccessor(Paths.get(dataPath));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
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



    public boolean sendRaw(String filename, byte[] data) throws RemoteException {
        if (rawTempPath == null)
            rawTempPath = createTemp("discodb_raw");
        return writeRaw(filename, data, rawTempPath);
    }

    /*public boolean sendRawToInsert(String filename, byte[] data) throws RemoteException {
        if(rawToInsert == null)
            rawToInsert = createTemp("discodb_to_insert");
        return writeRaw(filename, data, rawToInsert);
    }*/

    public Path createTemp(String name){
        try{
            return Files.createTempDirectory(name);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean writeRaw(String filename, byte[] data, Path dir){
        try {
            FileUtils.write(Paths.get(dir.toString(), filename), data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean distributeRaw() throws RemoteException {
        try {
            Files.walkFileTree(rawTempPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Peer[] peerList = peerObject.getPeers().toArray(new Peer[0]);
                    //String[] peerList = peerObject.getPeers()peers.keySet().toArray(new String[0]);
                    nextPeer++;
                    nextPeer = nextPeer % (peerList.length);
                    if (nextPeer < peerList.length) {
                        Peer p = peerList[nextPeer];
                        p.sendRawToInsert(file.getFileName().toString(), Files.readAllBytes(file));
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public InsertResult insert() throws RemoteException {
        for(Peer p : peerObject.getPeers()){
            lastInsert = p.insertLocal();
        }
        return lastInsert;
    }

    public InsertResult lastInsert() throws RemoteException {
        return lastInsert;
    }

    public void refresh(){
        try {
            ca = new CorpusAccessor(Paths.get(dataPath));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Result kwic(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException {
        System.out.println("Search for " + searchTerm);
        try {
            long start = System.currentTimeMillis();
            FullKwicResult fkr = ca.kwic(searchTerm, context, limit);
            lastResult = fkr;
            fkr.sort(sortType, sortPos, order);
            fkr.setPageLength(pageLength);
            long end = System.currentTimeMillis();
            fkr.setTime(end - start);
            return lastResult.it(ca);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result("Failed to find \"" + searchTerm + "\": " + e.getMessage());
        }
    }

    public Result list(String searchTerm) throws RemoteException {
        return null;
    }

    public Result it() throws RemoteException {
        return lastResult.it(ca);
    }
}
