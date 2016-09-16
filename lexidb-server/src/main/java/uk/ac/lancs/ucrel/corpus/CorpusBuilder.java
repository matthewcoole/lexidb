package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.region.RegionBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CorpusBuilder {

    private static final Logger LOG = LogManager.getLogger(CorpusBuilder.class);

    private Path corpusPath;
    private DecimalFormat regionNameFormatter;
    private int regionCount, totalCount;
    private Map<String, List<Integer>> index;
    private Dictionary d;
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
        LOG.debug("Region added with " + words.size() + " words.");
    }

    public void build() throws IOException {
        long start = System.currentTimeMillis();
        generateDict();
        generateIndexMappings();
        generateCorpusToRegionMappings();
        long end = System.currentTimeMillis();
        LOG.info("Corpus built in " + (end - start) + "ms");
    }

    public void save() throws IOException {
        long start = System.currentTimeMillis();

        FileUtils.write(Paths.get(corpusPath.toString(), "idx_ent.disco"), getIndexEntries());
        FileUtils.write(Paths.get(corpusPath.toString(), "idx_pos.disco"), indexMapping);

        d.save(createFile("dict.disco"));

        long end = System.currentTimeMillis();
        LOG.info("Corpus written in " + (end - start) + "ms");
    }

    private int[] getIndexEntries() {
        int[] entries = new int[totalCount];
        int i = 0;
        for (String s : d.getEntries()) {
            for (int n : index.get(s)) {
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
        index = new HashMap<String, List<Integer>>();
        d = new Dictionary();
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                File f = file.toFile();
                if (f.getName().equals("dict.disco")) {
                    int region = Integer.parseInt(f.getParentFile().getName());
                    List<String> words = Files.readAllLines(file, StandardCharsets.UTF_8);
                    for (String w : words) {
                        String[] ent = w.split("\t");
                        String word = w.substring(0, w.lastIndexOf("\t"));
                        int count = Integer.parseInt(ent[ent.length - 1]);
                        if (!index.containsKey(word)) {
                            index.put(word, new ArrayList<Integer>());
                        }
                        index.get(word).add(region);
                        d.putMany(word, count);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        d = Dictionary.sort(d);
    }

    private void generateIndexMappings() {
        indexMapping = new int[index.size() + 1];
        int pos = 0;
        for (int i = 0; i < indexMapping.length - 1; i++) {
            indexMapping[i] = pos;
            pos += index.get(d.get(i)).size();
        }
        indexMapping[indexMapping.length - 1] = pos;
        totalCount = pos;
    }

    private void generateCorpusToRegionMappings() throws IOException {
        for (int i = 0; i < regionCount; i++) {
            Dictionary rd = Dictionary.load(Paths.get(corpusPath.toString(), regionNameFormatter.format(i), "dict.disco"));
            int[] map = Dictionary.map(d, rd);
            FileUtils.write(Paths.get(corpusPath.toString(), regionNameFormatter.format(i), "map.disco"), map);
        }
    }

    private void delete() throws IOException {
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
