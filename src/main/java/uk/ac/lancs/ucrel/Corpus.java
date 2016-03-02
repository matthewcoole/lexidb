package uk.ac.lancs.ucrel;

import uk.ac.lancs.ucrel.region.Region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Corpus {

    private String name;
    private Path dataPath, corpusPath;
    private List<Region> regions;
    private DecimalFormat regionNameFormatter;

    public Corpus(String name, Path dataPath) throws IOException {
        this.name = name;
        this.dataPath = dataPath;
        corpusPath = Paths.get(dataPath.toString(), name);
        Files.createDirectories(corpusPath);
        regions = new ArrayList<Region>();
        regionNameFormatter = new DecimalFormat("0000");
    }

    public void add(List<String> words) throws IOException {
        getCurrentRegion().add(words);
    }

    private Region getCurrentRegion() throws IOException {
        if(regions.size() == 0)
            createRegion();
        return regions.get(regions.size() - 1);
    }

    private void createRegion() throws IOException {
        String name = regionNameFormatter.format(regions.size());
        regions.add(new Region(name, corpusPath));
    }

    public List<String> get(String word){
        return null;
    }
}
