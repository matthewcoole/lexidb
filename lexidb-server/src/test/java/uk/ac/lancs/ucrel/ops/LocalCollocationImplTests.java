package uk.ac.lancs.ucrel.ops;

import org.junit.Test;
import uk.ac.lancs.ucrel.ds.Collocate;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

public class LocalCollocationImplTests {
    @Test
    public void test() throws RemoteException {
        LocalCollocateOperationImpl col = new LocalCollocateOperationImpl(Paths.get("/home/mpc/data"));
        col.search(new String[]{"test"}, 1, 0, 10, false);
        List<Collocate> cols = col.it();
        for (Collocate c : cols) {
            System.out.println(c);
        }
        System.out.println("length=" + col.getLength() + ", time=" + col.getTime());
    }

}
