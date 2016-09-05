package uk.ac.lancs.ucrel.ops;

import org.junit.Test;
import uk.ac.lancs.ucrel.col.Collocate;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

public class LocalCollocationImplTests {
    @Test
    public void test() throws RemoteException {
        LocalCollocateImpl col = new LocalCollocateImpl(Paths.get("/home/mpc/data"));
        col.search("test", 1, 0, 10);
        List<Collocate> cols = col.it();
        for(Collocate c : cols){
            System.out.println(c);
        }
        System.out.println("length=" + col.getLength() + ", time=" + col.getTime());
    }

}
