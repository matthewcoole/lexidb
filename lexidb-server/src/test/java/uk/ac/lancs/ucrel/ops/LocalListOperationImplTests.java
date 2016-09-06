package uk.ac.lancs.ucrel.ops;

import org.junit.Test;
import uk.ac.lancs.ucrel.ds.WordListEntry;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

public class LocalListOperationImplTests {

    @Test
    public void test() throws RemoteException {
        LocalListOperationImpl l = new LocalListOperationImpl(Paths.get("/home/mpc/data"));
        l.search(".*", 20, false);
        List<WordListEntry> results = l.it();
        for(WordListEntry e : results){
            System.out.println(e);
        }
    }
}
