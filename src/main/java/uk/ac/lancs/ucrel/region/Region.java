package uk.ac.lancs.ucrel.region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Region {

    private Path corpusPath, regionPath;
    private Data data;
    private Dictionary dict;
    private Index idx;

    public Region(String name, Path corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        regionPath = Paths.get(corpusPath.toString(), name);
        Files.createDirectories(regionPath);
    }

    public void add(List<String> words) throws IOException {
        buildDictionary(words);
        buildDataAndIndex(words);
    }

    private void buildDataAndIndex(List<String> words) throws IOException {
        data = new Data("data.disco", regionPath);
        idx = new Index(regionPath);
        int pos = 0;
        for(String w : words){
            int val = dict.get(w);
            data.add(val);
            idx.add(val, pos++);
        }
        data.save();
        idx.save();
    }

    private void buildDictionary(List<String> words) throws IOException {
        dict = new Dictionary(regionPath);
        for(String w : words) {
            if (!dict.contains(w))
                dict.add(w);
        }
        dict.finalize();
    }
}
