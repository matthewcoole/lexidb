package uk.ac.lancs.ucrel.corpus;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class CorpusAccessorTest {

    @Test
    public void testSearch() throws IOException {
        CorpusAccessor ca = new CorpusAccessor(Paths.get("C:\\Users\\Matt\\Desktop\\lob_test"));
        ca.search("the", 20, false);
        ca.search("of", 20, false);
        ca.search("to", 20, false);
        ca.search("that", 20, false);
        ca.search("and", 20, false);
        ca.search("in", 20, false);
        ca.search("a", 20, false);
        ca.search("is", 20, false);
        ca.search("i", 20, false);
        ca.search("it", 20, false);
    }
}
