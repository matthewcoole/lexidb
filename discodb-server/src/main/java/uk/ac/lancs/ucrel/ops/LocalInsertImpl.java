package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.parser.TextParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;

public class LocalInsertImpl implements Insert {

    private ExecutorService es;
    private Path dataPath;
    private Path temp;
    private boolean complete = false;

    public LocalInsertImpl(ExecutorService es, Path dataPath) {
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
            FileUtils.write(Paths.get(temp.toString(), filename), data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void insert() throws RemoteException {
        es.execute(() -> insertRunner());
    }

    @Override
    public boolean isComplete() throws RemoteException {
        return complete;
    }

    private void insertRunner(){
        try {
            TextParser tp = new TextParser(dataPath);
            tp.parse(temp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        complete = true;
    }
}
