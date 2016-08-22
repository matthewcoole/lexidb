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

    private int[] corpusToRegionMap;
    private int[] regionToCorpusMap;

    public RegionAccessor(Path regionPath){
        setPath(regionPath);
    }

    public List<int[]> contextSearch(List<Integer> words, int left, int right) throws IOException {
        regenerateCorpusToRegionMap();
        List<Integer> numericValues = getNumericValues(words);
        List<IndexEntry> indexEntries = getIndexPositions(numericValues);
        getIndexEntryValues(indexEntries);
        return getContexts(getIndexValues(indexEntries), left, right);
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

    private List<Integer> getIndexValues(List<IndexEntry> indexEntries){
        List<Integer> allIndexValues = new ArrayList<Integer>();
        for(IndexEntry ie : indexEntries){
            allIndexValues.addAll(ie.getIndexValuesAsList());
        }
        return allIndexValues;
    }

    private List<int[]> getContexts(List<Integer> indexValues, int left, int right) throws IOException{
        Path dataFile = Paths.get(getPath().toString(), "data.disco");
        IntBuffer ib = FileUtils.readAllInts(dataFile);
        List<int[]> contexts = new ArrayList<int[]>();
        for(int i : indexValues){
            int[] context = new int[left + right + 1];
            int n = i - left;
            for(int j = 0; j < context.length; j++){
                try {
                    context[j] = regionToCorpusMap[ib.get(n++)];
                } catch (IndexOutOfBoundsException e){
                    context[j] = -1;
                }
            }
            contexts.add(context);
        }
        return contexts;
    }
}
