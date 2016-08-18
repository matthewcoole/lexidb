package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.result.FullKwicResult;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class LocalKwicImpl implements Kwic {

    private ExecutorService es;
    private Path dataPath;
    private FullKwicResult fkr;
    private long time;

    public LocalKwicImpl(ExecutorService es, Path dataPath){
        this.es = es;
        this.dataPath = dataPath;
    }

    @Override
    public void search(String searchTerm, int context, int limit, int sortType, int sortPos, int order, int pageLength) throws RemoteException {
        try {
            System.out.println("Search for " + searchTerm);
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            fkr = ca.kwic(searchTerm, context, limit);
            fkr.sort(sortType, sortPos, order);
            fkr.setPageLength(pageLength);
            long end = System.currentTimeMillis();
            time = end - start;
            fkr.setTime(end - start);
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    @Override
    public List<ConcordanceLine> it() throws RemoteException {
        try {
            return fkr.it(CorpusAccessor.getAccessor(dataPath));
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    public long getTime(){
        return time;
    }

    @Override
    public int getLength() throws RemoteException {
        return fkr.getResults().size();
    }
}
