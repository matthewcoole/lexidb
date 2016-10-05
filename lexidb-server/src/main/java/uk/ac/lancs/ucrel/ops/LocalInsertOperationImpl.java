package uk.ac.lancs.ucrel.ops;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.parser.TSVParser;
import uk.ac.lancs.ucrel.region.RegionAccessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

public class LocalInsertOperationImpl implements InsertOperation {

    private static Logger LOG = Logger.getLogger(LocalInsertOperationImpl.class);

    private ExecutorService es;
    private Path dataPath;
    private Path temp;
    private boolean complete = false;
    private int fileCount;
    private long start, end;

    public LocalInsertOperationImpl(ExecutorService es, Path dataPath, Path tmp) {
        this.es = es;
        this.dataPath = dataPath;
        try {
            Files.createDirectories(tmp);
            temp = Files.createTempDirectory(tmp, "discodb_raw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendRaw(String filename, byte[] data) throws RemoteException {
        try {
            if (fileCount == 0)
                start = System.currentTimeMillis();
            LOG.trace("Raw file received " + filename);
            FileUtils.write(Paths.get(temp.toString(), filename), data);
            fileCount++;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void insert() throws RemoteException {
        LOG.info("Inserting...");
        es.execute(() -> insertRunner());
    }

    @Override
    public boolean isComplete() throws RemoteException {
        return complete;
    }

    @Override
    public int getFileCount() throws RemoteException {
        return fileCount;
    }

    @Override
    public long getTime() throws RemoteException {
        return end - start;
    }

    private void insertRunner() {
        try {
            LOG.debug(" Inserting from " + temp.toString() + " to " + dataPath.toString());
            TSVParser tp = new TSVParser(dataPath);
            tp.parse(temp);
            FileUtils.closeAllFiles();
            FileUtils.openAllFiles(dataPath, "r");
            RegionAccessor.rebuildAllRegions(dataPath);
            CorpusAccessor.getAccessor(dataPath);
            end = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        complete = true;
    }
}
