package uk.ac.lancs.ucrel.ops;

import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ds.Ngram;

import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;

public class LocalNgramImplTests {

    @BeforeClass
    public static void setup() throws IOException {
        CorpusAccessor.getAccessor(Paths.get("/home/mpc/data"));
    }

    @Test
    public void test() throws RemoteException {
        LocalNgramOperationImpl ng = new LocalNgramOperationImpl(Paths.get("/home/mpc/data"));
        ng.search(new String[]{null, null, null, "nimoy"}, 5, 0, 10, false);
        List<Ngram> results = ng.it();
        for (Ngram ngg : results) {
            System.out.println(ngg);
        }
        System.out.println(ng.getTime() + "ms");
    }
}
