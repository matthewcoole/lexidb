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

    public LocalInsertOperationImpl(ExecutorService es, Path dataPath) {
        this.es = es;
        this.dataPath = dataPath;
        try {
            temp = Files.createTempDirectory("discodb_raw");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendRaw(String filename, byte[] data) throws RemoteException {
        try {
            LOG.trace("Raw file received " + filename);
            FileUtils.write(Paths.get(temp.toString(), filename), data);
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

    private void insertRunner() {
        try {
            LOG.debug(" Inserting from " + temp.toString() + " to " + dataPath.toString());
            TSVParser tp = new TSVParser(dataPath);
            tp.parse(temp);
            FileUtils.closeAllFiles();
            FileUtils.openAllFiles(dataPath, "r");
            RegionAccessor.rebuildAllRegions(dataPath);
            CorpusAccessor.getAccessor(dataPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        complete = true;
    }
}
