package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
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
    private static final int BUFFER_SIZE = 1024 * 256;

    private Path corpusPath;
    private DecimalFormat regionNameFormatter;
    private int regionCount, totalCount;
    private Map<String, List<Integer>> dict;
    private List<String> dictEntries;
    private int[] wordCount, indexMapping;


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
        long end = System.currentTimeMillis();
        LOG.info("Corpus built in " + (end - start) + "ms");
    }

    public void save() throws IOException {
        long start = System.currentTimeMillis();
        writeBinaryFile("idx_ent.disco", getIndexEntries());
        writeBinaryFile("idx_pos.disco", indexMapping);
        Files.write(createFile("dict.disco"), dictEntries, StandardCharsets.UTF_8);
        long end = System.currentTimeMillis();
        LOG.info("Corpus written in " + (end - start) + "ms");
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

    private void writeBinaryFile(String filename, int[] ints) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(createFile(filename)), BUFFER_SIZE));
        for(int n : ints){
            dos.writeInt(n);
        }
        dos.flush();
        dos.close();
    }

    private Path createFile(String filename) throws IOException {
        Path filePath = Paths.get(corpusPath.toString(), filename);
        Files.deleteIfExists(filePath);
        Files.createFile(filePath);
        return filePath;
    }

    private void generateDict() throws IOException {
        dict = new HashMap<String, List<Integer>>();
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                if(f.getName().equals("dict.disco")){
                    int region = Integer.parseInt(f.getParentFile().getName());
                    List<String> words = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for(String w : words){
                        if(!dict.containsKey(w))
                            dict.put(w, new ArrayList<Integer>());
                        dict.get(w).add(region);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        dictEntries = new ArrayList<String>(dict.keySet());
        Collections.sort(dictEntries);
    }

    private void generateMappings(){
        wordCount = new int[dict.size()];
        indexMapping = new int[dict.size()];
        int pos = 0;
        for(int i = 0; i < indexMapping.length; i++){
            indexMapping[i] = pos;
            pos += dict.get(dictEntries.get(i)).size();
        }
        totalCount = pos;
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
