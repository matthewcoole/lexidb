package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.file.system.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RegionBuilder {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);

    private Path regionPath;
    private List<String> words;
    private Dictionary d;
    private int[] data, indexMapping, initToFinalMap;
    private List<List<Integer>> index;
    private int totalCount;


    public RegionBuilder(Path regionPath) {
        this.regionPath = regionPath;
        words = new ArrayList<String>();
    }

    public void add(List<String> words) {
        this.words.addAll(words);
    }

    public void build() {
        init();
        assignInitNumericValues();
        generateInitToFinalMap();
        initIndex();
        assignFinalNumericValues();
        generateIndexMapping();
    }

    public void save() throws IOException {
        Files.createDirectories(regionPath);
        FileUtils.write(Paths.get(regionPath.toString(), "data.disco"), data);
        FileUtils.write(Paths.get(regionPath.toString(), "idx_ent.disco"), getIndexEntries());
        FileUtils.write(Paths.get(regionPath.toString(), "idx_pos.disco"), indexMapping);
        d.save(createFile("dict.disco"));
    }

    private Path createFile(String filename) throws IOException {
        Path filePath = Paths.get(regionPath.toString(), filename);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        return filePath;
    }

    private int[] getIndexEntries() {
        int[] entries = new int[totalCount];
        int i = 0;
        for (List<Integer> wordEntries : index) {
            for (Integer n : wordEntries) {
                entries[i] = n;
                i++;
            }
        }
        return entries;
    }

    private void init() {
        data = new int[words.size()];
        d = new Dictionary();
        totalCount = words.size();
    }

    private void assignInitNumericValues() {
        int i = 0;
        for (String word : words) {
            data[i] = d.put(stripSpaces(word));
            i++;
        }
    }

    private String stripSpaces(String s) {
        StringBuilder sb = new StringBuilder();
        String[] bits = s.split("\t");
        sb.append(bits[0]);
        for (int i = 1; i < bits.length; i++) {
            sb.append('\t').append(bits[i].trim());
        }
        return sb.toString();
    }

    private void generateInitToFinalMap() {
        Dictionary df = Dictionary.sort(d);
        initToFinalMap = Dictionary.map(d, df);
        d = df;
    }

    private void assignFinalNumericValues() {
        for (int i = 0; i < data.length; i++) {
            data[i] = initToFinalMap[data[i]];
            addIndexEntry(data[i], i);
        }
    }

    private void initIndex() {
        indexMapping = new int[d.size() + 1];
        index = new ArrayList<List<Integer>>(d.size());
        for (int i = 0; i < d.size(); i++) {
            index.add(new ArrayList<Integer>());
        }
    }

    private void addIndexEntry(int numericValue, int pos) {
        index.get(numericValue).add(pos);
    }

    private void generateIndexMapping() {
        int pos = 0;
        for (int i = 0; i < indexMapping.length - 1; i++) {
            indexMapping[i] = pos;
            pos += d.count(i);
        }
        indexMapping[indexMapping.length - 1] = pos;
    }
}
