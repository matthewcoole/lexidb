package server;

import io.javalin.Context;
import io.javalin.Javalin;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import properties.AppProperties;
import query.QueryProcessor;
import query.json.Query;
import result.Result;
import storage.Corpus;
import storage.DataBlock;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
public class Server {

    public Map<String, Corpus> toSave = new HashMap<>();
    public Javalin app;
    public QueryProcessor qp = new QueryProcessor();

    public Server(String propsPath) {
        log.info("Starting server...");
        long start = System.currentTimeMillis();
        if (propsPath != null) {
            AppProperties.loadProps(propsPath);
        }
        try {
            qp.loadAllCorpora(AppProperties.get("data.path"));
        } catch (Exception e) {
            log.error("Failed to load tables %s", e.getMessage());
            e.printStackTrace();
        }
        app = Javalin.create().port(1189).enableStaticFiles("/web").start();
        app.get("/api/test", ctx -> test(ctx));
        app.post("*/query", ctx -> query(ctx));
        app.post("*/create", ctx -> create(ctx));
        app.get("*/file/*", ctx -> file(ctx));
        app.get("*/save", ctx -> save(ctx));
        app.post("*/*/insert", ctx -> insert(ctx));
        app.get("*/size", ctx -> corpusSize(ctx));
        long end = System.currentTimeMillis();
        log.info("Server started in " + (end - start) + "ms.");
    }

    public static void main(String[] args) {
        new Server(args[0]);
    }

    @Generated
    public void file(Context ctx) throws Exception {
        long start = System.currentTimeMillis();
        String corpus = ctx.splat(0);
        String file = "/" + ctx.splat(1);
        Path p = Paths.get(AppProperties.get("data.path"), corpus);
        List<Map<String, String>> r = qp.getFile(file, p);
        ctx.json(r);
        long end = System.currentTimeMillis();
        log.info(String.format("Retrieved %s file from %s in %dms", file, corpus, (end - start)));
    }

    public List<Map<String, String>> file(String file, Path corpus) throws Exception {
        List<Map<String, String>> r = qp.getFile(file, corpus);
        for (Map<String, String> token : r) {
            if (token.containsKey("$file"))
                token.remove("$file");
        }
        return r;
    }

    public void stop() {
        app.stop();
    }

    @Generated
    public void corpusSize(Context ctx) throws ExecutionException {
        String corpus = ctx.splat(0);
        ctx.json(corpusSize(corpus));
    }

    public Map<String, Object> corpusSize(String corpus) throws ExecutionException {
        return qp.getCorpusSize(Paths.get(AppProperties.get("data.path"), corpus));
    }

    @Generated
    public void insert(Context ctx) throws IOException {
        String corpus = ctx.splat(0);
        String file = ctx.splat(1);
        String csv = new String(ctx.bodyAsBytes(), StandardCharsets.UTF_8);
        Iterable<CSVRecord> records = CSVFormat.TDF.withFirstRecordAsHeader().parse(new StringReader(csv));
        insert(corpus, file, records);
    }

    public void insert(String corpus, String file, Iterable<CSVRecord> records) {
        try {
            System.out.println("Inserting");
            if (!toSave.containsKey(corpus)) {
                Corpus c = new Corpus(corpus, AppProperties.get("data.path"));
                c.loadConf(Paths.get(AppProperties.get("data.path"), corpus, "conf.json").toString());
                toSave.put(corpus, c);
            }
            Corpus c = toSave.get(corpus);

            c.add(records, file);
            log.info("Insert successful!");
        } catch (Exception e) {
            log.error("INSERT FAILED!", e);
        }
    }

    @Generated
    public void save(Context ctx) throws IOException {
        String corpus = ctx.splat(0);
        save(corpus);
    }

    public void save(String corpus) throws IOException {
        toSave.get(corpus).save();
    }

    @Generated
    public void create(Context ctx) {
        String corpus = ctx.splat(0);
        DataBlock css = ctx.bodyAsClass(DataBlock.class);
        create(corpus, css);
    }

    public void create(String corpus, DataBlock css) {
        Response r = new Response();
        Corpus c = new Corpus(corpus, AppProperties.get("data.path"));
        try {
            c.setConf(css);
            toSave.put(corpus, c);
            r.success = true;
            log.info("Create successful!");
        } catch (IOException e) {
            log.error("CREATE FAILED!", e);
            r.success = false;
            r.message = e.getMessage();
        }
    }

    @Generated
    public void test(Context ctx) {
        ctx.json(new Test());
    }

    public Result query(String corpus, Query q) throws Exception {
        return qp.search(q, Paths.get(AppProperties.get("data.path"), corpus));
    }

    @Generated
    public void query(Context ctx) {
        String corpus = ctx.splat(0);
        try {
            long start = System.currentTimeMillis();
            Query q = ctx.bodyAsClass(Query.class);
            log.info(ctx.body());
            Result r = query(corpus, q);
            ctx.json(r);
            long end = System.currentTimeMillis();
            log.info("Query successful! [" + (end - start) + "ms]");
        } catch (Exception e) {
            log.error("QUERY FAILED!", e);
        }
    }

    public void mergeBlocks(String corpus, int a, int b) throws IOException, ExecutionException {
        Path p = Paths.get(AppProperties.get("data.path"), corpus);
        qp.mergeBlocks(p.toString(), a, b);
    }
}
