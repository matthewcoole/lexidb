import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import properties.AppProperties;
import query.json.Query;
import query.json.SortProperty;
import result.*;
import server.Server;
import storage.DataBlock;
import storage.JSONUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;

/**
 * Full integration test suite that creates a corpus, inserts 1 file of data, saves, performs a set of simple queries and checks the results.
 */
public class ServerIntegrationTests {

    private static final String CORPUS = "test";
    private static Server s;
    private Query q;

    @BeforeClass
    public static void setupClass() throws IOException {
        AppProperties.set("data.path", Files.createTempDirectory("lexi-data").toString());
        AppProperties.set("kwic.context", "5");
        AppProperties.set("result.page.size", "20");
        AppProperties.set("block.size", "1000");
        ClassLoader classLoader = ServerIntegrationTests.class.getClassLoader();
        Path p = Paths.get(classLoader.getResource("hansardSchema.json").getPath());
        s = new Server(null);
        DataBlock css = JSONUtil.loadColumnSuperSet(p);
        s.create(CORPUS, css);
        Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(Files.newBufferedReader(Paths.get(classLoader.getResource("hansardTestFile.tsv").getPath())));
        s.insert(CORPUS, "test-file", records);
        s.save(CORPUS);
    }

    @Before
    public void setup() {
        q = new Query();
        q.getQuery().put("tokens", "{\"token\":\"test\"}");
    }

    @Test
    public void testFile() throws Exception {
        Path p = Paths.get(AppProperties.get("data.path"), CORPUS);
        List<Map<String, String>> file = s.file("test-file", p);
        assertThat(file.size(), is(84597));
        assertThat(file.get(50).values(), hasItems("Formed", "VVN", "T2+"));
    }

    @Test
    public void kwicRegexTest() throws Exception {
        q.getQuery().put("tokens", "{\"token\":\"testi.*\"}");
        Result r = s.query(CORPUS, q);
        String results = ((KwicResultPage) r).toString("token");
        assertThat(results, is("to study for early years testing has really pushed the practical \n" +
                "accept that the emphasis on testing only English and mathsnot just \n" +
                "making learning fun , but testing is the building block that \n" +
                "I heard moving and powerful testimonies from Lyras family and members \n" +
                "cathedral , from the powerful testimony of Father Martin Magill , \n" +
                "There are hundreds of these testimonies , and I have highlighted \n" +
                "does call for a renewed testing framework , for fire sprinklers"));
    }

    @Test
    public void kwicSequenceTest() throws Exception {
        q.getQuery().put("tokens", "{\"token\":\"testing\"}{\"token\":\"is\"}");
        Result r = s.query(CORPUS, q);
        String results = ((KwicResultPage) r).toString("token");
        System.out.println(results);
        assertThat(results, is("making learning fun , but testing is the building block that allows"));
    }

    @Test
    public void kwicTest() throws Exception {
        Result r = s.query(CORPUS, q);
        String results = ((KwicResultPage) r).toString("token");
        assertThat(results, is("the key stage 2 reading test : 88% of those who"));
    }

    @Test
    public void kwicSortedTest() throws Exception {
        q.getQuery().put("tokens", "{\"token\":\"differently\"}");
        q.getResult().context = 5;
        SortProperty sp = new SortProperty();
        sp.setAlphabetical(true);
        sp.setAscending(false);
        sp.setColumn("token");
        sp.setPosition(-1);
        q.getResult().sort.add(sp);
        Result r = s.query(CORPUS, q);
        String results = ((KwicResultPage) r).toString("token");
        assertThat(results, is("why Belarus is being treated differently from Syria and Zimbabwe . \n" +
                "This process is currently done differently across the country , which"));
    }

    @Test
    public void ngramTest() throws Exception {
        q.getResult().type = "ngram";
        q.getResult().n = 2;
        q.getResult().context = 1;
        q.getResult().groupby = "token";
        Result r = s.query(CORPUS, q);
        String results = ((NgramResult) r).toString();
        assertThat(results, is("reading test: 1\ntest :: 1"));
    }

    @Test
    public void listTest() throws Exception {
        q.getResult().type = "list";
        q.getResult().context = 0;
        q.getResult().groupby = "token";
        Result r = s.query(CORPUS, q);
        String results = r.toString();
        assertThat(results, is("ListResultPage(list=[Pair(key=test, value=1)])"));
    }

    @Test
    public void collocationLogTest() throws Exception {
        q.getResult().type = "col-ll";
        q.getResult().context = 1;
        q.getResult().groupby = "token";
        Result r = s.query(CORPUS, q);
        String results = ((CollocationResultPage) r).toString();
        assertThat(results, is("CollocationResultPage(collocations=[Pair(key=reading, value={ll=17.420215595746413, a=1.0}), Pair(key=:, value={ll=7.472015602514148, a=1.0})])"));
    }

    @Ignore
    @Test
    public void collocationMiTest() throws Exception {
        q.getResult().type = "col-mi";
        q.getResult().context = 1;
        q.getResult().groupby = "token";
        Result r = s.query(CORPUS, q);
        String results = ((CollocationResult) r).toString();
        assertThat(results, is("reading[count: 1, # in corpus: 3, mutual information: 14.783356381867803 ]\n:[count: 1, # in corpus: 510, mutual information: 7.373965445730101 ]"));
    }

    @Test
    public void testCorpusSize() throws ExecutionException {
        Map<String, Object> result = s.corpusSize(CORPUS);
        assertThat(result.get("count"), is(84597));
    }
}
