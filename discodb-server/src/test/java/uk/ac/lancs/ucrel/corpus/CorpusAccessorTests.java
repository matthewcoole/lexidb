package uk.ac.lancs.ucrel.corpus;

import org.junit.Test;
import uk.ac.lancs.ucrel.result.FullKwicResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CorpusAccessorTests {

    @Test
    public void test() throws IOException {
        Path dataPath = Paths.get("/home/mpc/data");
        CorpusAccessor ca = new CorpusAccessor(dataPath);
        FullKwicResult fkr = ca.kwic("to", 10, 0);
        for(int[] line : fkr.getResults()){
            System.out.print("[");
            for(int i : line){
                System.out.print(i + "\t");
            }
            System.out.print("]\n");
            System.out.println(ca.getLineAsString(line));
        }
    }
}
