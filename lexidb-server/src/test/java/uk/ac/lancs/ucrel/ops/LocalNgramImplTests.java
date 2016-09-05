package uk.ac.lancs.ucrel.ops;

import org.junit.Test;
import uk.ac.lancs.ucrel.ngram.NGram;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

public class LocalNgramImplTests {

    @Test
    public void test() throws RemoteException {
        LocalNgramImpl ng = new LocalNgramImpl(Paths.get("/home/mpc/data"));
        ng.search("the", 2, 0, 10);
        List<NGram> results = ng.it();
        for(NGram ngg : results){
            System.out.println(ngg);
        }
        System.out.println(ng.getTime());
    }
}
