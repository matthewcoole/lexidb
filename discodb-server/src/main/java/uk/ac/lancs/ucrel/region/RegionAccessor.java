package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.index.IndexEntry;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionAccessor extends Accessor {

    private static final Logger LOG = LogManager.getLogger(RegionAccessor.class);
    private int context = 5;

    private int[] corpusToRegionMap;
    private int[] regionToCorpusMap;

    public RegionAccessor(Path regionPath){
        setPath(regionPath);
    }

    public List<int[]> search(int word, int context, int limit) throws IOException {
        List<Integer> words = new ArrayList<Integer>();
        words.add(word);
        return search(words, context, limit);
    }

    public List<int[]> search(List<Integer> words, int context, int limit) throws IOException {
        this.context = context;
        regenerateCorpusToRegionMap();
        List<Integer> numericValues = getNumericValues(words);
        List<IndexEntry> indexEntries = getIndexPositions(numericValues);
        getIndexEntryValues(indexEntries);
        return getConcordanceLines(indexEntries, limit);
    }

    private void getIndexEntryValues(List<IndexEntry> indexEntries) throws IOException {
        for(IndexEntry i : indexEntries){
            getIndexEntryValues(i);
        }
    }

    private List<Integer> getNumericValues(List<Integer> words) {
        List<Integer> numericValues = new ArrayList<Integer>();
        for(int i : words){
            int val = corpusToRegionMap[i];
            if(val > - 1)
                numericValues.add(val);
        }
        return numericValues;
    }

    private List<IndexEntry> getIndexPositions(List<Integer> numericValues) throws IOException {
        List<IndexEntry> indexEntries = new ArrayList<IndexEntry>();
        for(int i : numericValues){
            indexEntries.add(getIndexPos(i));
        }
        return indexEntries;
    }

    private void regenerateCorpusToRegionMap() throws IOException {
        Path mapFile = Paths.get(getPath().toString(), "map.disco");
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

    private List<int[]> getConcordanceLines(List<IndexEntry> indexEntries, int limit) throws IOException {
        List<int[]> concLines = new ArrayList<int[]>();
        List<Integer> allIndexValues = new ArrayList<Integer>();
        int count = 0;
        for(IndexEntry ie : indexEntries){
            allIndexValues.addAll(ie.getIndexValuesAsList());
            count += ie.getCount();
        }
        concLines.addAll(getConcordanceLines(allIndexValues, count, limit));
        return concLines;
    }

    private List<int[]> getConcordanceLines(List<Integer> indexValues, int count, int limit) throws IOException {
        Path dataFile = Paths.get(getPath().toString(), "data.disco");
        IntBuffer ib = FileUtils.readAllInts(dataFile);

        limit = (limit > count || limit < 1) ? count : limit;

        List<int[]> concLines = new ArrayList<int[]>();

        for(int i = 0; i < limit; i++){
            int[] line = new int[(context * 2) + 1];
            int n = indexValues.get(i) - context;
            for(int j = 0; j < line.length; j++){
                try {
                    line[j] = regionToCorpusMap[ib.get(n++)];
                } catch (IndexOutOfBoundsException e){
                    //ignore if we fall off the end of the region
                }
            }
            concLines.add(line);
        }

        return concLines;
    }
}
