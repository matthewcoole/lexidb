package uk.ac.lancs.ucrel;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class CorpusTest {
    @Test
    public void test() throws IOException {
        Corpus c = new Corpus("test", Paths.get("C:\\Users\\Matt\\Desktop\\data_dump"));
        List<String> words = Arrays.asList("this", "is", "a", "list", "of", "words", "to", "add", "to", "the", "corpus");
        c.add(words);
        System.out.println(c.get("list"));
    }
}
