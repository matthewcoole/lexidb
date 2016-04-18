package uk.ac.lancs.ucrel;

import static org.junit.Assert.*;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.parser.TextParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SimpleIT {

    @ClassRule
    public static TemporaryFolder DATA = new TemporaryFolder();

    public String[] expectedResults = {"switched off the shop lights and closed the shutters; but so", "paleness onto the wet pavement, and it was while I was", "it. I closed the door and put the shop key in"};

    private CorpusAccessor ca;

    @BeforeClass
    public static void parseInData() throws IOException {
        TextParser tp = new TextParser(Paths.get(DATA.getRoot().getPath()));
        tp.parse(Paths.get(new File("src/test/resources/test_corpus").getAbsolutePath()));
    }

    @Before
    public void setup() throws IOException {
        ca = new CorpusAccessor(Paths.get(DATA.getRoot().getPath()));
    }

    @Test
    public void basicLookup() throws IOException {
        List<String> results = ca.search("and", 3);
        assertThat(results, IsIterableContainingInOrder.contains(expectedResults));
    }
}
