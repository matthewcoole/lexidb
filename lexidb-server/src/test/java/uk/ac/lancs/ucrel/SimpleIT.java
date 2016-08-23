package uk.ac.lancs.ucrel;

import static org.junit.Assert.*;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import uk.ac.lancs.ucrel.parser.TextParser;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.KwicResult;
import uk.ac.lancs.ucrel.rmi.result.Result;
import uk.ac.lancs.ucrel.server.ServerImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class SimpleIT {

    @ClassRule
    public static TemporaryFolder DATA = new TemporaryFolder();

    public static Server s;

    public String[] expectedResults = {"new slogan jones is best and the fact that jones is", "manufacturers who wanted their children and their childrens children to reap", "himself in his own lifetime and let his brats and brats"};

    @BeforeClass
    public static void parseInData() throws IOException {
        TextParser tp = new TextParser(Paths.get(DATA.getRoot().getPath()));
        tp.parse(Paths.get(new File("src/test/resources/test_corpus").getAbsolutePath()));
        Properties p = new Properties();
        p.setProperty("server.data.path", Paths.get(DATA.getRoot().getPath()).toString());

        //s = new ServerImpl(p.getProperty("server.data.path"));
    }

    @Ignore
    @Test
    public void basicLookup() throws IOException {
        /*Result r = s.context("and", 5, 3, 0, 0, 0, 20);
        KwicResult kr = null;
        if(r instanceof KwicResult)
            kr = (KwicResult)r;
        List<String> results = kr.getPage();
        assertThat(results, IsIterableContainingInOrder.contains(expectedResults));*/
    }
}
