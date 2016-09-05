package uk.ac.lancs.ucrel.parser;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TSVParserTests {

    @Test
    public void test() throws IOException {
        Path dataPath = Paths.get("/home/mpc/data");
        TSVParser p = new TSVParser(dataPath);
        p.parse(Paths.get("/home/mpc/bnc_tag"));
    }
}
