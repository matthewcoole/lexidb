package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RegionBuilder {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);
    private static final int BUFFER_SIZE = 1024 * 256;

    private Path regionPath;
    private List<String> words;
    private SortedMap<String, Integer> dict;
    private int[] data;
    private int[] wordCount;
    private int[] indexMapping;
    private int[] initToFinalMap;
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
        assignInitNumericValues();
        generateInitToFinalMap();
        initIndex();
        assignFinalNumericValues();
        generateIndexMapping();
        long end = System.currentTimeMillis();
        LOG.info("Built region in " + (end - start) + "ms");
        LOG.trace(this);
    }

    public void save() throws IOException {
        long start = System.currentTimeMillis();
        Files.createDirectories(regionPath);
        writeBinaryFile("data.disco", data);
        writeBinaryFile("idx_ent.disco", getIndexEntries());
        writeBinaryFile("idx_pos.disco", indexMapping);
        Files.write(createFile("dict.disco"), dict.keySet(), StandardCharsets.UTF_8);
        long end = System.currentTimeMillis();
        LOG.info("Region written in " + (end - start) + "ms");
    }

    private void writeBinaryFile(String filename, int[] ints) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(createFile(filename)), BUFFER_SIZE));
        for(int n : ints){
            dos.writeInt(n);
        }
        dos.flush();
        dos.close();
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
        dict = new TreeMap<String, Integer>();
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
        for(String word : dict.keySet()){
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
        index = new ArrayList<List<Integer>>();
        for(int n : initToFinalMap){
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
