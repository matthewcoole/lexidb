package uk.ac.lancs.ucrel.ops;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import uk.ac.lancs.ucrel.ds.Kwic;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public class LocalKwicOperationImplTests {
    @Before
    public void setup() throws RemoteException {
        LocalKwicOperationImpl kop = new LocalKwicOperationImpl(Paths.get("/home/mpc/data"));
        //kop.newSearch(Arrays.asList("test", "NN1", "SUBST", "test"), 5, 0, 0, 0, false, 20);
    }

    @Test
    public void test() throws RemoteException {
        LocalKwicOperationImpl kop = new LocalKwicOperationImpl(Paths.get("/home/mpc/data"));
        //kop.newSearch(Arrays.asList("zombie.*", null, null, null), 5, 0, 0, 0, false, 20);
        System.out.println("tag search " + kop.getLength() + " results in " + kop.getTime() + "ms");
    }

    @Test
    public void oldtest() throws RemoteException {
        LocalKwicOperationImpl kop = new LocalKwicOperationImpl(Paths.get("/home/mpc/data"));
        kop.search("zombie.*", 5, 0, 0, 0, false, 20);
        System.out.println("old search " + kop.getLength() + " results in " + kop.getTime() + "ms");
    }
}
