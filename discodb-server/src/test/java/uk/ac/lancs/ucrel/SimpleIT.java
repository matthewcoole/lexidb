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

    public String[] expectedResults = {"new slogan jones is best and the fact that jones is", "manufacturers who wanted their children and their childrens children to reap", "himself in his own lifetime and let his brats and brats"};

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
        List<int[]> raw_results = ca.kwic("and", 5, 3).getResults();
        List<String> results = ca.getLinesAsString(raw_results);
        assertThat(results, IsIterableContainingInOrder.contains(expectedResults));
    }
}
