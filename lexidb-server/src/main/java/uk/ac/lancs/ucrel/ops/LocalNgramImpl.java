package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class LocalNgramImpl implements Ngram {

    private ExecutorService es;
    private Path dataPath;

    public LocalNgramImpl(ExecutorService es, Path dataPath){
        this.es = es;
        this.dataPath = dataPath;
    }

    @Override
    public void search(String searchTerm, int n) throws RemoteException {

    }

    @Override
    public List<ConcordanceLine> it() throws RemoteException {
        return null;
    }

    @Override
    public int getLength() throws RemoteException {
        return 0;
    }

    @Override
    public long getTime() throws RemoteException {
        return 0;
    }
}
