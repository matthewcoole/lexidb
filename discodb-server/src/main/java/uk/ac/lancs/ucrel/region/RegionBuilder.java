package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.file.system.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionBuilder {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);

    private Path regionPath;
    private List<String> words, finalDictEntries;
    private Dictionary d;
    private int[] data, wordCount, indexMapping, initToFinalMap;
    private List<List<Integer>> index;
    private int totalCount;


    public RegionBuilder(Path regionPath){
        this.regionPath = regionPath;
        words = new ArrayList<String>();
    }

    public void add(List<String> words){
        this.words.addAll(words);
    }

    public void build(){
        long start = System.currentTimeMillis();
        init();
        long a = System.currentTimeMillis();
        assignInitNumericValues();
        long b = System.currentTimeMillis();
        generateInitToFinalMap();
        long c = System.currentTimeMillis();
        initIndex();
        long d = System.currentTimeMillis();
        assignFinalNumericValues();
        long e = System.currentTimeMillis();
        generateIndexMapping();
        long end = System.currentTimeMillis();
        LOG.info("Region built in " + (end - start) + "ms (" + (a - start) + "ms init, " + (b - a) + "ms initVals, " + (c-b) + "ms initToFinalMapGen, " + (d-c) + "ms initIndex, " + (e-d) + "ms finalNumerics, " + (end - e) + "ms indexMapping)");
        LOG.trace(this);
    }

    public void save() throws IOException {
        long start = System.currentTimeMillis();
        Files.createDirectories(regionPath);

        FileUtils.write(Paths.get(regionPath.toString(), "data.disco"), data);
        FileUtils.write(Paths.get(regionPath.toString(), "idx_ent.disco"), getIndexEntries());
        FileUtils.write(Paths.get(regionPath.toString(), "idx_pos.disco"), indexMapping);

        generateFinalDictionaryEntries();

        Files.write(createFile("dict.disco"), finalDictEntries, StandardCharsets.UTF_8);
        long end = System.currentTimeMillis();
        LOG.info("Region written in " + (end - start) + "ms");
    }

    public void generateCorpusToRegionMap(List<String> corpusDict, Map<String, Integer> regionDict) throws IOException {
        int[] map = new int[corpusDict.size()];
        for(int i = 0; i < corpusDict.size(); i++){
            String s = corpusDict.get(i);
            int n = -1;
            if(regionDict.containsKey(s))
                n = regionDict.get(s);
            map[i] = n;
        }

        FileUtils.write(Paths.get(regionPath.toString(), "map.disco"), map);
    }

    private Path createFile(String filename) throws IOException {
        Path filePath = Paths.get(regionPath.toString(), filename);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        return filePath;
    }

    private int[] getIndexEntries(){
        int[] entries = new int[totalCount];
        int i = 0;
        for(List<Integer> wordEntries : index){
            for(Integer n : wordEntries){
                entries[i] = n;
                i++;
            }
        }
        return entries;
    }

    private void init(){
        data = new int[words.size()];
        d = new Dictionary();
        totalCount = words.size();
    }

    private void assignInitNumericValues(){
        int i = 0;
        for(String word : words){
            data[i] = d.put(word);
            i++;
        }
    }

    private void generateInitToFinalMap(){
        Dictionary df = Dictionary.sort(d);
        initToFinalMap = Dictionary.map(d, df);
        d = df;
    }

    private void assignFinalNumericValues(){
        for(int i = 0; i < data.length; i++){
            data[i] = initToFinalMap[data[i]];
            addIndexEntry(data[i], i);
            wordCount[data[i]] += 1;
        }
    }

    private void initIndex(){
        wordCount = new int[d.size()];
        indexMapping = new int[d.size()];
        index = new ArrayList<List<Integer>>(d.size());
        for(int i = 0; i < d.size(); i++){
            index.add(new ArrayList<Integer>());
        }
    }

    private void addIndexEntry(int numericValue, int pos){
        index.get(numericValue).add(pos);
    }

    private void generateIndexMapping(){
        int pos = 0;
        for(int i = 0; i < indexMapping.length; i++){
            indexMapping[i] = pos;
            pos += wordCount[i];
        }
    }

    private void generateFinalDictionaryEntries(){
        finalDictEntries = new ArrayList<String>();
        for(int i = 0; i < d.size(); i++){
            finalDictEntries.add(d.get(i) + " " + wordCount[i]);
        }
    }
}
