package query;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import dk.brics.automaton.RunAutomaton;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.math.stats.LogLikelihood;
import properties.AppProperties;
import query.json.Query;
import query.json.SortProperty;
import result.*;
import storage.Corpus;
import storage.DataBlock;
import util.ArrayUtils;
import util.Pair;
import util.QueryKey;
import util.SortedSkimmer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

import static java.lang.Math.log;

@Slf4j
public class QueryProcessor {

    private ThreadPoolExecutor executor =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    private transient LoadingCache<String, Corpus> corpusCache = CacheBuilder.newBuilder().maximumSize(Integer.parseInt(AppProperties.get("corpus.cache.size"))).build(new CacheLoader<String, Corpus>() {
        @Override
        public Corpus load(String path) {
            return Corpus.loadCorpus(path);
        }
    });

    private transient LoadingCache<QueryKey, Result> resultsCache = CacheBuilder.newBuilder().maximumSize(Integer.parseInt(AppProperties.get("result.cache.size"))).expireAfterAccess(Integer.parseInt(AppProperties.get("result.cache.timeout")), TimeUnit.MINUTES).build(new CacheLoader<QueryKey, Result>() {
        @Override
        public Result load(QueryKey key) throws Exception {
            return generateResult(key.getQuery(), key.getDataPath());
        }
    });

    public Map<String, Object> getCorpusSize(Path dataPath) throws ExecutionException {
        Corpus c = corpusCache.get(dataPath.toString());
        return c.corpusSize();
    }

    public Result generateResult(Query q, Path dataPath) throws IOException, ExecutionException {
        if (q.getResult().type.equals("kwic"))
            return generateKwicResult(q, dataPath);
        if (q.getResult().type.equals("list"))
            return generateListResult(q, dataPath);
        if(q.getResult().type.startsWith("col"))
            return generateCollocations(q, dataPath);
        else
            return generateKwicResult(q, dataPath);
    }

    public List<int[]> search(String query, DataBlock css, int blockNumber) throws IOException {
        TokenQuery tq = new TokenQuery(query);
        Map<Character, int[]> positionLookup = new HashMap<>();
        for (char c : tq.getQbeObjects().keySet()) {
            Map<String, String> qbe = tq.getQbeObjects().get(c);

            //have to create a list of sets in the query here otherwise problems arise when no numeric value is found
            Set<String> sets = new HashSet<>();
            for(String column : qbe.keySet()){
                sets.add(css.memberOfSet(column));
            }

            Map<String, List<Integer>> numericValues = css.lookupNumericValues(qbe);
            int[] allPositions = null;
            long start = System.currentTimeMillis();
            for (String set : sets) {
                int[] positions = new int[0];
                if(numericValues.containsKey(set))
                    positions = css.lookupPositions(numericValues.get(set), set);
                if (allPositions == null)
                    allPositions = positions;
                else
                    allPositions = ArrayUtils.intersectSortedArrays(allPositions, positions);
            }
            if (allPositions == null)
                allPositions = new int[]{};
            positionLookup.put(c, allPositions);
            long end = System.currentTimeMillis();
            log.info("Index lookup took: " + (end - start) + "ms");
        }

        List<int[]> matches;
        if (tq.getQueryString().length() == 1 && positionLookup.size() == 1) {
            long a = System.currentTimeMillis();
            Character c = positionLookup.keySet().iterator().next();
            int[] positions = positionLookup.get(c);
            matches = new ArrayList<>(positions.length);
            for (int i = 0; i < positions.length; i++) {
                int[] match = {blockNumber, positions[i], positions[i]};
                matches.add(match);
            }
            long b = System.currentTimeMillis();
            log.info("Direct match results generated in " + (b - a) + "ms");
        } else {
            long a = System.currentTimeMillis();
            SortedSkimmer sk = newSkimmer(positionLookup);
            matches = run(tq.getQuery(), sk, blockNumber);
            long b = System.currentTimeMillis();
            log.info("Running regex over token stream took: " + (b - a) + "ms");
        }
        return matches;
    }

    private SortedSkimmer newSkimmer(Map<Character, int[]> lookup) {
        SortedSkimmer sk = new SortedSkimmer();
        for (char c : lookup.keySet()) {
            sk.add(lookup.get(c), c);
        }
        return sk;
    }

    private List<int[]> run(RunAutomaton ra, SortedSkimmer sk, int blockNumber) {
        List<int[]> matches = new ArrayList<>(1000000);
        int startState = ra.getInitialState();
        while (sk.next()) {
            int startIndex = sk.getPos();
            int currentIndex = startIndex;
            int currentState = startState;
            int endIndex = -1;
            sk.mark();
            while (currentIndex < startIndex + 10) {
                char nextChar = (currentIndex == sk.getPos()) ? sk.getChar() : '?';
                currentState = ra.step(currentState, nextChar);
                if (currentState == -1)
                    break;
                if (ra.isAccept(currentState))
                    endIndex = currentIndex;
                if (currentIndex == sk.getPos())
                    sk.next();
                currentIndex++;
            }
            if (endIndex != -1)
                matches.add(new int[]{blockNumber, startIndex, endIndex});
            sk.reset();
        }
        return matches;
    }

    public void loadAllCorpora(String dataPath) throws Exception {
        for (File f : Paths.get(dataPath).toFile().listFiles()) {
            if (f.isDirectory()) {
                for (File c : f.listFiles()) {
                    if (c.isDirectory()) {
                        loadCorpora(Paths.get(f.getPath()));
                    }
                }
            }
        }
    }


    private void loadCorpora(Path dataPath) throws ExecutionException {
        corpusCache.get(dataPath.toString());
    }

    public Result search(Query q, Path dataPath) throws Exception {
        List<Result> results = new ArrayList<>();
        QueryKey key = new QueryKey(dataPath, q);
        if (q.getResult().type.equals("kwic")) {
            result.Result r = getKwicResult(key);
            results.add(r);
        }
        if (q.getResult().type.equals("ngram")) {
            result.Result r = getKwicResult(key);
            results.add(generateNgrams(key));
        }
        if (q.getResult().type.equals("list")) {
            results.add(getListResult(key));
        }
        if (q.getResult().type.startsWith("col")) {
            //result.Result r = getKwicResult(key);
            result.Result r = getCollocationResult(key);
            results.add(r);
        }
        return results.get(0);
    }

    public List<Map<String, String>> getFile(String file, Path dataPath) throws Exception {
        Corpus c = corpusCache.get(dataPath.toString());
        for (int i = 0; i < c.numberOfBlocks(); i++) {
            DataBlock b = c.loadBlock(i);
            List<int[]> matches = search(String.format("{\"$file\":\"%s\"}", file), b, i);
            if (matches.size() > 0) {
                List<Integer> positions = new ArrayList<>(matches.size());
                for (int[] j : matches) {
                    positions.add(j[1]);
                }
                return b.get(positions);
            }
        }
        return null;
    }

    private Result generateCollocations(Query q, Path dataPath) throws IOException, ExecutionException {
        Corpus c = corpusCache.get(dataPath.toString());
        CollocationResult r = new CollocationResult();
        r.setTotalBlocks(c.numberOfBlocks());

        if (q.getResult().type.endsWith("ll")) {
            if (q.getResult().async)
                executor.submit(() -> collocationStatsForBlocks(c, q, r));
            else {
                collocationStatsForBlocks(c, q, r);
            }
        }

        return r;
    }

    private Map<String, Map<String, Double>> collocationStatsForBlocks(Corpus corpus, Query q, CollocationResult cr) throws ExecutionException, IOException {
        Map<String, Map<String, Double>> stats = new HashMap<>();
        Map<String, Integer> c = new HashMap<>();
        int d = 0;
        int b = 0;
        int corpusSize = corpus.finalCount();
        for(int i = 0; i < corpus.numberOfBlocks(); i++){
            cr.setBlockQueried(i+1);
            DataBlock css = corpus.loadBlock(i);
            List<int[]> matches = search(q.getQuery().get(css.name), css, i);
            List<List<Map<String, String>>> kwics = css.getList(corpus.generateContextPositions(matches, q.getResult().context), "token");
            Map<String, Integer> a = getStatsFromKwic(kwics, q.getResult().context);
            b += matches.size();
            d += css.finalCount();

            for(String s : a.keySet()){
                if(!c.containsKey(s)){
                    c.put(s, (int)corpus.finalCount("token", s));
                }
                if(!stats.containsKey(s)){
                    Map<String, Double> emptyStat = new HashMap<>();
                    emptyStat.put("a", 0.0);
                    emptyStat.put("ll", 0.0);
                    stats.put(s, emptyStat);
                }
                double scalar = (double)d/(double)corpusSize;
                loglikelihood(a.get(s), b, (double)c.get(s) * scalar, d, stats.get(s));
            }
            List<Pair<String, Map<String, Double>>> cols = new ArrayList<>();
            for(String s : stats.keySet()){
                cols.add(new Pair(s, stats.get(s)));
            }
            Collections.sort(cols, new Comparator<Pair<String, Map<String, Double>>>() {
                @Override
                public int compare(Pair<String, Map<String, Double>> o1, Pair<String, Map<String, Double>> o2) {
                    return Double.compare((double) o2.getValue().get("ll"), (double) o1.getValue().get("ll"));
                }
            });
            cr.setCollocations(cols);
        }
        cr.setSorted(true);
        return stats;
    }

    private void loglikelihood(double a, double b, double c, double d, Map<String, Double> currentStats){
        currentStats.replace("a", currentStats.get("a") + a);
        a = currentStats.get("a");
        double ll = 2*(a*log(a) + b*log(b) + c*log(c) + d*log(d)
                - (a+b)*log(a+b) - (a+c)*log(a+c)
                - (b+d)*log(b+d) - (c+d)*log(c+d)
                + (a+b+c+d)*log(a+b+c+d));
        currentStats.replace("ll", ll);
    }

    private Map<String, Integer> getFreq(Set<String> words, DataBlock css) {
        Map<String, Integer> stats = new HashMap<>();
        for(String s : words){
            stats.putIfAbsent(s, css.finalCount("token", s));
        }
        return stats;
    }

    private Map<String, Integer> getStatsFromKwic(List<List<Map<String, String>>> kwics, int context) {
        Map<String, Integer> stats = new HashMap<>();
        for(List<Map<String, String>> kwic : kwics){
            for(int i = 0; i < context; i++){
                String word = kwic.get(i).get("token");
                stats.putIfAbsent(word, 0);
                stats.replace(word, stats.get(word) + 1);
            }
            for(int i = kwic.size() - context; i < kwic.size(); i++){
                String word = kwic.get(i).get("token");
                stats.putIfAbsent(word, 0);
                stats.replace(word, stats.get(word) + 1);
            }
        }
        return stats;
    }

    private double L(double k, double n, double x) {
        return Math.pow(x, k) * Math.pow((1 - x), (n - k));
    }

    private Map<String, Integer> getCounts(KwicResultPage krp, Query q) {
        Map<String, Integer> counts = new HashMap<>();
        for (List<Map<String, String>> concordance : krp.getConcordances()) {
            for (int i = 0; i < q.getResult().context; i++) {
                String token = concordance.get(i).get("token");
                if (token.charAt(0) == '<' && token.endsWith(">"))
                    continue;
                if (!counts.containsKey(token))
                    counts.put(token, 0);
                counts.replace(token, counts.get(token) + 1);
            }
            for (int i = concordance.size() - q.getResult().context; i < concordance.size(); i++) {
                String token = concordance.get(i).get("token");
                if (token.charAt(0) == '<' && token.endsWith(">"))
                    continue;
                if (!counts.containsKey(token))
                    counts.put(token, 0);
                counts.replace(token, counts.get(token) + 1);
            }
        }
        return counts;
    }

    /*private CollocationResult mutualInformation(Corpus c, KwicResultPage krp, Query q) throws ExecutionException {
        CollocationResult cr = new CollocationResult();
        Map<String, Integer> counts = getCounts(krp, q);
        double corpusSize = c.finalCount();
        double px = krp.getConcordances().size() / corpusSize;
        for (String key : counts.keySet()) {
            Map<String, String> token = new HashMap<>();
            token.put("token", key);
            Map<String, Object> info = new HashMap<>();
            int xycount = counts.get(key);
            info.put("count", counts.get(key));
            int ycount = c.finalCount(token);
            info.put("# in corpus", ycount);

            double pxy = xycount / corpusSize;
            double py = ycount / corpusSize;

            double ll = pxy / (px * py);

            ll = Math.log(ll) / Math.log(2);

            info.put("mutual information", ll);

            cr.collocates.add(new Pair(key, info));
        }
        cr.hitCount = krp.getConcordances().size();
        cr.setContext(q.getResult().context);
        Collections.sort(cr.collocates, new Comparator<Pair<String, Map<String, Object>>>() {
            @Override
            public int compare(Pair<String, Map<String, Object>> o1, Pair<String, Map<String, Object>> o2) {
                return Double.compare((double) o1.getValue().get("mutual information"), (double) o2.getValue().get("mutual information"));
            }
        });
        Collections.reverse(cr.collocates);
        return cr;
    }*/

    private Result generateListResult(Query q, Path dataPath) throws IOException, ExecutionException {
        ListResult lr = new ListResult();

        TokenQuery tq = new TokenQuery(q.getQuery().get("tokens"));
        Map<String, String> firstItem = tq.getQbeObjects().values().iterator().next();
        String firstKey = firstItem.keySet().iterator().next();
        if (q.getResult().groupby.isEmpty())
            q.getResult().groupby = firstKey;

        if (tq.getQbeObjects().size() == 1 && firstItem.size() == 1 && q.getResult().context == 0 && q.getResult().groupby.equals(firstKey)) {
            Corpus c = corpusCache.get(dataPath.toString());
            Map<String, Integer> freq = c.frequencyList(firstItem, q.getResult().groupby);
            for (String k : freq.keySet()) {
                lr.list.add(new Pair<>(k, freq.get(k)));
            }
        } else {
            log.info("Dynamic frequency list");
            log.error("Not currently supported");
        }
        Collections.sort(lr.list, Comparator.comparingInt(Pair::getValue));
        Collections.reverse(lr.list);
        lr.setSorted(true);
        return lr;
    }

    private NgramResult generateNgrams(QueryKey key) throws IOException, ExecutionException {
        NgramResult nr = new NgramResult();
        KwicResult kr = (KwicResult) resultsCache.get(key);
        long start = System.currentTimeMillis();
        KwicResultPage krp = kr.getAllResults(key.getQuery().getResult().groupby);
        long end = System.currentTimeMillis();
        log.info("Retrieved all data in " + (end - start) + "ms");
        Map<String, Integer> ngramMap = new HashMap<>();

        for (List<Map<String, String>> concordance : krp.getConcordances()) {
            for (int i = 0; i <= concordance.size() - key.getQuery().getResult().n; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < key.getQuery().getResult().n; j++) {
                    sb.append(concordance.get(j + i).get(key.getQuery().getResult().groupby));
                    sb.append(' ');
                }
                String ngram = sb.toString().trim();
                if (!ngramMap.containsKey(ngram)) {
                    ngramMap.put(ngram, 0);
                }
                ngramMap.replace(ngram, ngramMap.get(ngram) + 1);
            }
        }

        for (String nkey : ngramMap.keySet()) {
            nr.ngrams.add(new Pair(nkey, ngramMap.get(nkey)));
        }
        Collections.sort(nr.ngrams, Comparator.comparingInt(Pair::getValue));
        Collections.reverse(nr.ngrams);
        nr.setSorted(true);
        return nr;
    }

    private ListResultPage getListResult(QueryKey key) throws ExecutionException {
        ListResult r = (ListResult) resultsCache.get(key);
        ListResultPage lrp = r.getPage(key.getQuery().getResult().page, key.getQuery().getResult().pageSize);
        return lrp;
    }

    private KwicResult generateKwicResult(Query q, Path dataPath) throws ExecutionException, IOException {
        Corpus c = corpusCache.get(dataPath.toString());
        KwicResult r = new KwicResult();
        r.setCorpus(c);
        r.setTotalBlocks(c.numberOfBlocks());
        if(q.getResult().async)
            executor.submit(() -> queryBlocks(q, c, r));
        else {
            queryBlocks(q, c, r);
            r.sort(q.getResult().sort.toArray(new SortProperty[]{}));
        }
        return r;
    }

    private void queryBlocks(Query q, Corpus c, KwicResult r) {
        try {
            for (int i = 0; i < c.numberOfBlocks(); i++) {
                DataBlock css = c.loadBlock(i);
                List<int[]> matches = search(q.getQuery().get(css.name), css, i);
                r.addAllToLookup(matches);
                r.setContext(q.getResult().context);
                r.setBlockQueried(i+1);
            }
            r.sort(q.getResult().sort.toArray(new SortProperty[]{}));
            r.setSorted(true);
            log.debug("Sorted");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private KwicResultPage getKwicResult(QueryKey key) throws IOException, ExecutionException {
        KwicResult r = (KwicResult) resultsCache.get(key);
        long a = System.currentTimeMillis();
        KwicResultPage krp = r.getPage(key.getQuery().getResult().page, key.getQuery().getResult().pageSize, key.getQuery().getResult().context);
        long b = System.currentTimeMillis();
        log.info("Retrieved page in: " + (b - a) + "ms");
        return krp;
    }

    private CollocationResultPage getCollocationResult(QueryKey key) throws IOException, ExecutionException {
        CollocationResult r = (CollocationResult) resultsCache.get(key);
        long a = System.currentTimeMillis();
        CollocationResultPage crp = r.getPage(key.getQuery().getResult().page, key.getQuery().getResult().pageSize);
        long b = System.currentTimeMillis();
        log.info("Retrieved page in: " + (b - a) + "ms");
        return crp;
    }

    public void mergeBlocks(String dataPath, int a, int b) throws ExecutionException, IOException {
        Corpus c = corpusCache.get(dataPath);
        c.mergeBlocks(a, b);
    }
}
