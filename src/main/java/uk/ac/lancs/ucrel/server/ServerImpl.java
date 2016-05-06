package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.parser.TextParser;
import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.KwicResult;
import uk.ac.lancs.ucrel.rmi.result.Result;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.sort.ConcLineComparator;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerImpl implements Server {

    public boolean shutdown = false;
    private String dataPath = "/home/mpc/Desktop/data";
    private CorpusAccessor ca;
    private List<int[]> last;
    private int lastPos, lastContext, lastPageLength, lastSort, lastSize, lastLimit;
    private long lastTime;
    private String lastKeyword;

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
            lastTime = (end - start);
            return new InsertResult("Inserted " + path, (end - start));
        } catch(Exception e){
            List<String> errors = new ArrayList<String>();
            errors.add(e.getMessage());
            errors.add(e.getCause().getMessage());
            return new Result("Failed to insert \"" + path + "\"");
        }
    }

    public Result kwic(String keyword, int context, int limit, int sort, int order, int pageLength) throws RemoteException {
        System.out.println("Search for " + keyword);
        try {
            long start = System.currentTimeMillis();
            this.lastKeyword = keyword;
            this.lastContext = context;
            this.lastLimit = limit;
            this.lastSort = sort;
            this.lastPageLength = pageLength;
            last = searchResults(keyword, context, limit);
            lastPos = 0;
            if(sort != 0){
                Collections.sort(last, new ConcLineComparator(context, sort));
                if(order < 0)
                    Collections.reverse(last);
            }
            long end = System.currentTimeMillis();
            lastTime = end - start;
            return getResults();

        } catch (Exception e){
            return new Result("Failed to find \"" + keyword + "\"");
        }
    }

    public Result it() throws RemoteException {
        return getResults();
    }

    private List<int[]> searchResults(String keyword, int context, int limit){
        try {
            return ca.search(keyword, context, limit);
        } catch(Exception e){
            e.printStackTrace();
            return new ArrayList<int[]>();
        }
    }

    private Result getResults(){
        List<String> page = new ArrayList<String>();
        for(int i = lastPos; i < last.size() && i < (lastPos + lastPageLength); i++){
            page.add(ca.getLineAsString(last.get(i)));
        }
        lastPos += lastPageLength;
        String header = "Found " + last.size() + " for \"" + lastKeyword + "\"";
        if(lastSort != 0)
            header += " (sorted on " + lastSort + ")";
        return new KwicResult(header, lastTime, page, (lastPos - lastPageLength + 1), lastPos, last.size(), lastContext);
    }
}
