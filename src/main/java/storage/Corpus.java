package storage;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import properties.AppProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Corpus {
    private DecimalFormat df = new DecimalFormat("0000");
    private String[] columns;
    private String name;
    private Path path, confPath;
    private transient LoadingCache<Integer, DataBlock> blocksCache = CacheBuilder.newBuilder().maximumSize(Integer.parseInt(AppProperties.get("block.cache.size"))).build(new CacheLoader<Integer, DataBlock>() {
        @Override
        public DataBlock load(Integer key) throws Exception {
            Path p = Paths.get(path.toString(), df.format(key), "conf.json");
            long a = System.currentTimeMillis();
            DataBlock css = JSONUtil.loadColumnSuperSet(p);
            css.setPath(p.getParent());
            css.load();
            css.corpus = p.getParent().getParent().getFileName().toString();
            long b = System.currentTimeMillis();
            log.info(String.format("Loading block %s took; %dms", p.toString(), (b - a)));
            return css;
        }
    });
    private int lastBlockId;
    private DataBlock lastBlock;

    public Corpus(String name, String path) {
        this.name = name;
        this.path = Paths.get(path, name);
        confPath = Paths.get(this.path.toString(), ".conf.json");
    }

    public static Corpus loadCorpus(String path) {
        Path p = Paths.get(path);
        Corpus c = new Corpus(p.toFile().getName(), p.getParent().toString());
        c.loadConf(Paths.get(path, ".conf.json").toString());
        return c;
    }

    public void halfBlocks() throws IOException {
        int lowestBlock = 0;
        int highestBlock = numberOfBlocks() - 1;
        for (int i = 0; i < highestBlock; i += 2) {
            mergeBlocks(i, i + 1);
            removeBlock(i);
            removeBlock(i + 1);
            renameMergeBlock(i, "-merge", lowestBlock++);
        }
        if (highestBlock % 2 == 0)
            renameMergeBlock(highestBlock, "", lowestBlock);
    }

    public void cascadeMerge(int numberOfBlocks) throws IOException {
        if (numberOfBlocks < 1)
            numberOfBlocks = 1;
        while (numberOfBlocks() > numberOfBlocks) {
            halfBlocks();
        }
    }

    public void renameMergeBlock(int i, String append, int l) throws IOException {
        Path p = Paths.get(path.toString(), df.format(i) + append);
        Path o = Paths.get(path.toString(), df.format(l));
        FileUtils.moveDirectory(p.toFile(), o.toFile());
    }

    public void removeBlock(int i) throws IOException {
        Path pathA = Paths.get(path.toString(), df.format(i));
        FileUtils.deleteDirectory(pathA.toFile());
    }

    public void mergeBlocks(int a, int b) throws IOException {
        Path pathA = Paths.get(path.toString(), df.format(a));
        Path pathB = Paths.get(path.toString(), df.format(b));
        DataBlock blockC = JSONUtil.loadColumnSuperSet(confPath);
        blockC.setPath(Paths.get(pathA + "-merge"));
        DataBlock blockA = JSONUtil.loadColumnSuperSet(confPath);
        blockA.setPath(pathA);
        DataBlock blockB = JSONUtil.loadColumnSuperSet(confPath);
        blockB.setPath(pathB);
        MergeTask t = new MergeTask(blockA, blockB, blockC);
        t.run();
    }

    public DataBlock loadBlock(int n) throws ExecutionException {
        return blocksCache.get(n);
    }

    public int numberOfBlocks() {
        return highestBlockNumber() + 1;
    }

    public void loadConf(String confPath) {
        try {
            DataBlock css = JSONUtil.loadColumnSuperSet(Paths.get(confPath));
            setConf(css);
        } catch (Exception e) {
            log.error("Failed to load conf " + confPath);
        }
    }

    public void reloadConf() {
        confPath = Paths.get(path.toString(), ".conf.json");
    }

    public void setConf(DataBlock css) throws IOException {
        Path p = Paths.get(path.toString(), ".conf.json");
        path.toFile().mkdirs();
        path.toFile().mkdir();
        JSONUtil.savetoJSON(p, css);
        this.columns = css.listAllColumns();
        this.confPath = p;
    }

    public void addFiles(String dir, String... filePatterns) throws IOException {
        if (lastBlock == null) {
            nextBlock();
        }
        System.out.println("Finding files...");
        Collection<File> filesToAdd = FileUtils.listFiles(new File(dir), filePatterns, true);
        List<File> fileList = new ArrayList<>();
        fileList.addAll(filesToAdd);
        Collections.sort(fileList, Comparator.comparing(File::toString));
        for (File f : ProgressBar.wrap(fileList, "Adding files")) {
            lastBlock.addFile(f, dir);
            if (lastBlock.size() >= Integer.parseInt(AppProperties.get("block.size"))) {
                lastBlock.complete();
                lastBlock.save();
                nextBlock();
            }
        }
    }

    public void add(Iterable<CSVRecord> records, String file) throws IOException {
        if (lastBlock == null) {
            nextBlock();
        }
        lastBlock.add(records, file);
        if (lastBlock.size() >= Integer.parseInt(AppProperties.get("block.size"))) {
            lastBlock.complete();
            lastBlock.save();
            nextBlock();
        }
    }

    private void nextBlock() throws IOException {
        lastBlock = JSONUtil.loadColumnSuperSet(confPath);
        lastBlockId = highestBlockNumber() + 1;
        lastBlock.setPath(Paths.get(path.toString(),df.format(lastBlockId)));
    }

    private int highestBlockNumber() {
        if (path.toFile().listFiles().length > 1) {
            String[] fileNames = path.toFile().list();
            Arrays.sort(fileNames);
            String last = fileNames[fileNames.length - 1];
            return Integer.parseInt(last);
        }
        return -1;
    }

    public void save() throws IOException {
        if (lastBlock.size() > 0) {
            lastBlock.complete();
            lastBlock.save();
        }
    }

    public List<String> lookup(List<int[]> rowIndices, String column) throws ExecutionException, IOException {
        List<String> rows = new ArrayList<>();
        Map<Integer, List<int[]>> blocksToRow = new HashMap<>();
        for (int[] row : rowIndices) {
            if (!blocksToRow.containsKey(row[0]))
                blocksToRow.put(row[0], new ArrayList<>());
            blocksToRow.get(row[0]).add(row);
        }
        Map<int[], String> rowToValue = new HashMap<>();
        for (Integer i : blocksToRow.keySet()) {
            DataBlock css = blocksCache.get(i);
            List<List<Map<String, String>>> list = css.getList(generateContextPositions(blocksToRow.get(i), 0), column);
            int j = 0;
            for (int[] row : blocksToRow.get(i)) {
                rowToValue.put(row, list.get(j).get(0).get(column));
                j++;
            }
        }
        for (int[] row : rowIndices) {
            rows.add(rowToValue.get(row));
        }
        return rows;
    }

    public List<List<Map<String, String>>> retrieveConcordances(List<int[]> hits, int context) throws ExecutionException, IOException {
        return retrieveConcordances(hits, context, this.columns);
    }

    public List<List<Integer>> generateContextPositions(List<int[]> matches, int context) {
        List<List<Integer>> positions = new ArrayList<>();
        for (int[] m : matches) {
            List<Integer> pos = new ArrayList<>();
            for (int j = m[1] - context; j <= m[2] + context; j++) {
                pos.add(j);
            }
            positions.add(pos);
        }
        return positions;
    }

    public List<List<Map<String, String>>> retrieveConcordances(List<int[]> hits, int context, String[] columns) throws ExecutionException, IOException {
        List<List<Map<String, String>>> concordances = new ArrayList<>();
        Map<Integer, List<int[]>> hitsToBlocks = new HashMap<>();
        for (int[] hit : hits) {
            if (!hitsToBlocks.containsKey(hit[0])) {
                hitsToBlocks.put(hit[0], new ArrayList());
            }
            hitsToBlocks.get(hit[0]).add(hit);
        }
        for (Integer i : hitsToBlocks.keySet()) {
            DataBlock css = blocksCache.get(i);
            concordances.addAll(css.getList(generateContextPositions(hitsToBlocks.get(i), context), columns));
        }
        return concordances;
    }

    public Map<String, Integer> frequencyList(Map<String, String> searchTerm, String column) throws ExecutionException {
        Map<String, Integer> totalFrequencies = new HashMap<>();
        for (int i = 0; i < numberOfBlocks(); i++) {
            DataBlock css = blocksCache.get(i);
            String set = css.memberOfSet(column);
            Map<String, List<Integer>> vals = css.lookupNumericValues(searchTerm);
            int[] frequencies = css.getFrequencyList(vals.get(set), set);
            String[] dataValues = css.getValues(vals.get(set), set);
            for (int j = 0; j < dataValues.length; j++) {
                if (!totalFrequencies.containsKey(dataValues[j]))
                    totalFrequencies.put(dataValues[j], 0);
                totalFrequencies.replace(dataValues[j], totalFrequencies.get(dataValues[j]) + frequencies[j]);
            }
        }
        return totalFrequencies;
    }

    public int finalCount() {
        int finalCount = 0;
        for (int i = 0; i < numberOfBlocks(); i++) {
            try {
                DataBlock css = blocksCache.get(i);
                finalCount += css.finalCount();
            } catch (ExecutionException e) {
                log.error(e.getMessage());
            }
        }
        return finalCount;
    }

    public int finalCount(Map<String, String> token) {
        int finalCount = 0;
        for (int i = 0; i < numberOfBlocks(); i++) {
            try {
                DataBlock css = blocksCache.get(i);
                finalCount += css.finalCount(token);
            } catch (ExecutionException e) {
                log.error(e.getMessage());
            }
        }
        return finalCount;
    }

    public long finalCount(String token, String key) {
        long finalCount = 0;
        for (int i = 0; i < numberOfBlocks(); i++) {
            try {
                DataBlock css = blocksCache.get(i);
                finalCount += css.finalCount(token, key);
            } catch (ExecutionException e) {
                log.error(e.getMessage());
            }
        }
        return finalCount;
    }

    public Map<String, Object> corpusSize() {
        Map<String, Object> result = new HashMap<>();
        result.put("count", 0);
        Map<String, Integer> types = new HashMap<>();
        result.put("types", types);
        for (int i = 0; i < numberOfBlocks(); i++) {
            try {
                DataBlock b = blocksCache.get(i);
                Map<String, Object> blockStats = b.corpusSize();
                result.replace("count", (Integer) result.get("count") + (Integer) blockStats.get("count"));
                Map<String, Integer> blockTypes = (Map<String, Integer>) blockStats.get("types");
                for (String key : blockTypes.keySet()) {
                    if (!types.containsKey(key))
                        types.put(key, 0);
                    types.replace(key, types.get(key) + blockTypes.get(key));
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
