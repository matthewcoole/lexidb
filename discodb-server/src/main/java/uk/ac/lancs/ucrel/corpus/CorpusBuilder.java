package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.region.RegionAccessor;
import uk.ac.lancs.ucrel.region.RegionBuilder;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;

public class CorpusBuilder {

    private static final Logger LOG = LogManager.getLogger(CorpusBuilder.class);

    private Path corpusPath;
    private DecimalFormat regionNameFormatter;
    private int regionCount, totalCount;
    private Map<String, List<Integer>> dict;
    private Map<String, Integer> dictWordCount;
    private List<String> dictEntries, finalDictEntries;
    private int[] indexMapping;


    public CorpusBuilder(Path corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        regionNameFormatter = new DecimalFormat("0000");
        Files.createDirectories(corpusPath);
        delete();
    }

    public void addRegion(List<String> words) throws IOException {
        RegionBuilder rb = new RegionBuilder(Paths.get(corpusPath.toString(), regionNameFormatter.format(regionCount++)));
        rb.add(words);
        rb.build();
        rb.save();
    }

    public void build() throws IOException {
        long start = System.currentTimeMillis();
        generateDict();
        generateMappings();
        generateCorpusToRegionMappings();
        long end = System.currentTimeMillis();
        LOG.info("Corpus built in " + (end - start) + "ms");
    }

    public void save() throws IOException {
        long start = System.currentTimeMillis();

        FileUtils.write(Paths.get(corpusPath.toString(), "idx_ent.disco"), getIndexEntries());
        FileUtils.write(Paths.get(corpusPath.toString(), "idx_pos.disco"), indexMapping);

        generateFinalDict();
        Files.write(createFile("dict.disco"), finalDictEntries, StandardCharsets.UTF_8);
        long end = System.currentTimeMillis();
        LOG.info("Corpus written in " + (end - start) + "ms");
    }

    private void generateFinalDict(){
        finalDictEntries = new ArrayList<String>();
        for(String s : dictEntries){
            finalDictEntries.add(s + " " + dictWordCount.get(s));
        }
    }

    private int[] getIndexEntries(){
        int[] entries = new int[totalCount];
        int i = 0;
        for(String s : dictEntries){
            for(int n : dict.get(s)){
                entries[i++] = n;
            }
        }
        return entries;
    }

    private Path createFile(String filename) throws IOException {
        Path filePath = Paths.get(corpusPath.toString(), filename);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        return filePath;
    }

    private void generateDict() throws IOException {
        dict = new HashMap<String, List<Integer>>();
        dictWordCount = new HashMap<String, Integer>();
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                if(f.getName().equals("dict.disco")){
                    int region = Integer.parseInt(f.getParentFile().getName());
                    List<String> words = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for(String w : words){
                        String[] ent = w.split(" ");
                        String word = ent[0];
                        int count = Integer.parseInt(ent[1]);
                        if(!dict.containsKey(word)) {
                            dict.put(word, new ArrayList<Integer>());
                            dictWordCount.put(word, 0);
                        }
                        dict.get(word).add(region);
                        int currentCount = dictWordCount.get(word);
                        dictWordCount.put(word, (currentCount + count));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        dictEntries = new ArrayList<String>(dict.keySet());
        Collections.sort(dictEntries);
    }

    private void generateMappings(){
        indexMapping = new int[dict.size()];
        int pos = 0;
        for(int i = 0; i < indexMapping.length; i++){
            indexMapping[i] = pos;
            pos += dict.get(dictEntries.get(i)).size();
        }
        totalCount = pos;
    }

    private void generateCorpusToRegionMappings() throws IOException {
        long start = System.currentTimeMillis();
        for(int i = 0; i < regionCount; i++){
            RegionBuilder rb = new RegionBuilder(Paths.get(corpusPath.toString(), regionNameFormatter.format(i)));
            RegionAccessor ra = new RegionAccessor(Paths.get(corpusPath.toString(), regionNameFormatter.format(i)));
            rb.generateCorpusToRegionMap(dictEntries, ra.getDict());
        }
        long end = System.currentTimeMillis();
        LOG.debug("Corpus to region maps generated in " + (end - start) + "ms");
    }

    private void delete() throws IOException {
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException
            {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
