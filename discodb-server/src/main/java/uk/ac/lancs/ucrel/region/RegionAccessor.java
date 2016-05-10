package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.file.system.FileUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionAccessor {

    private static final Logger LOG = LogManager.getLogger(RegionAccessor.class);
    private int context = 5;

    private Path regionPath;
    private Map<String, Integer> dict;
    private int[] corpusToRegionMap;
    private int[] regionToCorpusMap;
    private List<String> map;
    int numericValue, indexPos, count, limit;
    private int[] indexEntries;
    private List<int[]> concLines;

    public RegionAccessor(Path regionPath){
        this.regionPath = regionPath;
    }

    public List<int[]> search(int word, int context, int limit) throws IOException {
        this.limit = limit;
        this.context = context;
        long start = System.currentTimeMillis();
        regenerateCorpusToRegionMap();
        long end = System.currentTimeMillis();
        LOG.debug("Corpus to region map generation: " + (end - start) + "ms");
        numericValue = corpusToRegionMap[word];
        LOG.debug(word + " -> " + numericValue + " : " + regionPath.toString());
        getIndexPos();
        LOG.debug(indexPos);
        getIndexEntries();
        getConcordanceLines();
        return concLines;
    }

    public Map<String, Integer> getDict() throws IOException {
        if(dict == null)
            regenerateDict();
        return dict;
    }

    private void regenerateCorpusToRegionMap() throws IOException {
        Path mapFile = Paths.get(regionPath.toString(), "map.disco");
        IntBuffer ib = FileUtils.readAllInts(mapFile);
        corpusToRegionMap = new int[ib.limit()];
        ib.get(corpusToRegionMap);
        regionToCorpusMap = new int[corpusToRegionMap.length];
        int n = 0;
        for(int i : corpusToRegionMap){
            if(i >= 0)
                regionToCorpusMap[i] = n;
            n++;
        }
    }

    private void regenerateDict() throws IOException {
        dict = new HashMap<String, Integer>();
        map = new ArrayList<String>();
        List<String> words = Files.readAllLines(Paths.get(regionPath.toString(), "dict.disco"), StandardCharsets.UTF_8);
        int i = 0;
        for(String s : words){
            String word = s.split(" ")[0];
            map.add(word);
            dict.put(word, i++);
        }
    }

    private void getIndexPos() throws IOException {
        Path indexPosFile = Paths.get(regionPath.toString(), "idx_pos.disco");
        IntBuffer ib = FileUtils.readInts(indexPosFile, numericValue, 2);
        indexPos = ib.get(0);
        count = ib.get(1) - indexPos;
    }

    private void getIndexEntries() throws IOException {
        Path indexEntFile = Paths.get(regionPath.toString(), "idx_ent.disco");
        IntBuffer ib = FileUtils.readInts(indexEntFile, indexPos, count);
        indexEntries = new int[ib.limit()];
        ib.get(indexEntries);
    }

    private void getConcordanceLines() throws IOException {
        Path dataFile = Paths.get(regionPath.toString(), "data.disco");
        IntBuffer ib = FileUtils.readAllInts(dataFile);

        limit = (limit > count || limit < 1) ? count : limit;

        concLines = new ArrayList<int[]>();

        for(int i = 0; i < limit; i++){
            int[] line = new int[(context * 2) + 1];
            int n = indexEntries[i] - context;
            for(int j = 0; j < line.length; j++){
                try {
                    line[j] = regionToCorpusMap[ib.get(n++)];
                } catch (IndexOutOfBoundsException e){
                    //ignore if we fall off the end of the region
                }
            }
            concLines.add(line);
        }
    }
}
