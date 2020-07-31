import org.junit.Before;
import org.junit.Test;
import properties.AppProperties;
import server.Server;
import util.Insert;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class UtilIntegrationTests {

    private static final String CORPUS = "test";
    private static Server s;

    @Before
    public void setupClass() throws IOException {
        AppProperties.set("data.path", Files.createTempDirectory("lexi-data").toString());
        AppProperties.set("kwic.context", "5");
        AppProperties.set("result.page.size", "10");
        AppProperties.set("block.size", "3");
        s = new Server(null);
    }

    @Test
    public void insertTest() throws IOException, ExecutionException {
        ClassLoader classLoader = ServerIntegrationTests.class.getClassLoader();
        String confPath = classLoader.getResource("simpleSchema.json").getFile();
        String dir = classLoader.getResource("testFiles").getFile();
        new Insert().insert(CORPUS, confPath, dir);
        Map<String, Object> result = s.corpusSize(CORPUS);
        assertThat(result.get("count"), is(19));
    }

    @Test
    public void mergeTest() throws IOException, ExecutionException {
        insertTest();
        s.mergeBlocks(CORPUS, 0, 1);
        Map<String, Object> result = s.corpusSize(CORPUS);
        assertThat(result.get("count"), is(19));
    }
}
