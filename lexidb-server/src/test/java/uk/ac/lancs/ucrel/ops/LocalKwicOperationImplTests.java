package uk.ac.lancs.ucrel.ops;

import org.junit.Test;
import uk.ac.lancs.ucrel.ds.Kwic;

import java.nio.file.Paths;
import java.rmi.RemoteException;

public class LocalKwicOperationImplTests {

    @Test
    public void test() throws RemoteException {
        LocalKwicOperationImpl kop = new LocalKwicOperationImpl(Paths.get("/home/mpc/data_new"));
        kop.search(new String[]{"the", null, null, null}, 5, 0, 0, 0, false, 20);
        for(Kwic k : kop.it()){
            System.out.println(k);
        }
        System.out.println("tag search " + kop.getLength() + " results in " + kop.getTime() + "ms");
    }
}
