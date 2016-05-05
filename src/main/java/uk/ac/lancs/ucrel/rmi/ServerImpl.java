package uk.ac.lancs.ucrel.rmi;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.parser.TextParser;
import uk.ac.lancs.ucrel.sort.ConcLineComparator;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerImpl implements Server {

    public boolean shutdown = false;
    private String dataPath = "/home/mpc/Desktop/data";
    private CorpusAccessor ca;

    public ServerImpl(){
        try {
            ca = new CorpusAccessor(Paths.get(dataPath));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public boolean isShutdown() throws RemoteException {
        return shutdown;
    }

    public void shutdown() throws RemoteException {
        shutdown = true;
    }

    public Result insert(String path) throws RemoteException {
        System.out.println("Inserting from " + path);
        try {
            long start = System.currentTimeMillis();
            TextParser tp = new TextParser(Paths.get(dataPath));
            tp.parse(Paths.get(path));
            ca = new CorpusAccessor(Paths.get(dataPath));
            long end = System.currentTimeMillis();
            return new Result("Inserted " + path, (end - start), new ArrayList<String>());
        } catch(Exception e){
            List<String> errors = new ArrayList<String>();
            errors.add(e.getMessage());
            errors.add(e.getCause().getMessage());
            return new Result("Failed to insert \"" + path + "\"", 0, errors);
        }
    }

    public Result search(String keyword) throws RemoteException {
        System.out.println("Search for " + keyword);
        try {
            long start = System.currentTimeMillis();
            List<int[]> results = searchResults(keyword);
            long end = System.currentTimeMillis();

            return new Result("Found " + results.size() + " for \"" + keyword + "\"", (end - start), getResults(results, 20));

        } catch (Exception e){
            List<String> errors = new ArrayList<String>();
            errors.add(e.getMessage());
            errors.add(e.getCause().getMessage());
            return new Result("Failed to find \"" + keyword + "\"", 0, errors);
        }
    }

    public Result search(String keyword, int sort){
        System.out.println("Search for " + keyword + " sorted on " + sort);
        try {
            long start = System.currentTimeMillis();
            List<int[]> results = searchResults(keyword);
            Collections.sort(results, new ConcLineComparator(sort));
            long end = System.currentTimeMillis();

            return new Result("Found " + results.size() + " for \"" + keyword + "\" (sorted on " + sort + ")", (end - start), getResults(results, 20));

        } catch(Exception e){
            List<String> errors = new ArrayList<String>();
            errors.add(e.getMessage());
            errors.add(e.getCause().getMessage());
            return new Result("Failed to find \"" + keyword + "\"", 0, errors);
        }
    }

    private List<int[]> searchResults(String keyword){
        try {
            return ca.search(keyword, 0);
        } catch(Exception e){
            e.printStackTrace();
            return new ArrayList<int[]>();
        }
    }

    private List<String> getResults(List<int[]> results, int n){
        List<String> page = new ArrayList<String>();
        for(int i = 0; i < results.size() && i < n; i++){
            page.add(ca.getLineAsString(results.get(i)));
        }
        return page;
    }
}
