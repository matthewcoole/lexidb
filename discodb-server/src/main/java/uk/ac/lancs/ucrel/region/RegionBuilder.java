package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.file.system.FileUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionBuilder {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);

    private Path regionPath;
    private List<String> words, dictEntries, finalDictEntries;
    private Map<String, Integer> dict;
    private int[] data, wordCount, indexMapping, initToFinalMap;
    private List<List<Integer>> index;
    private int typeCount, totalCount;


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
        dict = new HashMap<String, Integer>();
        typeCount = 0;
        totalCount = words.size();
    }

    private void assignInitNumericValues(){
        int i = 0;
        for(String word : words){
            if(!dict.containsKey(word))
                dict.put(word, typeCount++);
            data[i] = dict.get(word);
            i++;
        }
    }

    private void generateInitToFinalMap(){
        initToFinalMap = new int[dict.size()];
        int i = 0;
        dictEntries = new ArrayList<String>(dict.keySet());
        Collections.sort(dictEntries);
        for(String word : dictEntries){
            initToFinalMap[dict.get(word)] = i++;
        }
    }

    private void assignFinalNumericValues(){
        for(int i = 0; i < data.length; i++){
            data[i] = initToFinalMap[data[i]];
            addIndexEntry(data[i], i);
            wordCount[data[i]] += 1;
        }
    }

    private void initIndex(){
        wordCount = new int[dict.size()];
        indexMapping = new int[dict.size()];
        index = new ArrayList<List<Integer>>(dict.size());
        for(int i = 0; i < dict.size(); i++){
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
        for(int i = 0; i < dictEntries.size(); i++){
            finalDictEntries.add(dictEntries.get(i) + " " + wordCount[i]);
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("dict: ");
        sb.append(dict.toString());
        sb.append(", initToFinalMap: {");
        if(initToFinalMap != null){
            for(int n : initToFinalMap){
                sb.append(n).append(", ");
            }
        }
        sb.append("}, data: {");
        if(data != null){
            for(int n : data){
                sb.append(n).append(", ");
            }
        }
        sb.append("}, index: {");
        if(index != null){
            sb.append(index.toString());
        }
        sb.append("}, indexMapping: {");
        if(indexMapping != null){
            for(int n : indexMapping){
                sb.append(n).append(", ");
            }
        }
        sb.append("}, wordCount: {");
        if(wordCount != null){
            for(int n : wordCount){
                sb.append(n).append(", ");
            }
        }
        sb.append("}}");
        return sb.toString();
    }

}