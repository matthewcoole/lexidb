package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.index.IndexEntry;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionAccessor extends Accessor {

    private static final Logger LOG = LogManager.getLogger(RegionAccessor.class);

    private static Map<String, RegionAccessor> accessors = new HashMap<String, RegionAccessor>();

    private int[] corpusToRegionMap;
    private int[] regionToCorpusMap;

    public RegionAccessor(Path regionPath) throws IOException {
        setPath(regionPath);
        regenerateCorpusToRegionMap();
    }

    public static RegionAccessor getAccessor(Path regionPath) throws IOException {
        if (!accessors.containsKey(regionPath.toString())) {
            accessors.put(regionPath.toString(), new RegionAccessor(regionPath));
        }
        return accessors.get(regionPath.toString());
    }

    public static void rebuildAllRegions(Path corpusPath) throws IOException {
        String[] files = corpusPath.toFile().list();
        for (String f : files) {
            Path p = Paths.get(corpusPath.toString(), f);
            if (Files.isDirectory(p))
                getAccessor(p);
        }
    }

    public List<int[]> contextSearch(List<Integer> words, int left, int right) throws IOException {
        long a = System.currentTimeMillis();
        List<Integer> numericValues = getNumericValues(words);
        long b = System.currentTimeMillis();
        Collection<IndexEntry> indexEntries = getIndexPos(numericValues).values();
        long c = System.currentTimeMillis();
        getIndexEntryValues(indexEntries);
        long d = System.currentTimeMillis();
        List<int[]> contexts = getContexts(getIndexValues(indexEntries), left, right);
        long e = System.currentTimeMillis();
        LOG.trace("Contexts for region retrieved in " + (e - a) + "ms [getNumericValues= " + (b - a) + "ms, getIndexPositions= " + (c - b) + "ms, getIndexEntryValues= " + (d - c) + "ms, getContexts=" + (e - d) + "ms]");
        return contexts;
    }

    private List<Integer> getNumericValues(List<Integer> words) {
        List<Integer> numericValues = new ArrayList<Integer>();
        for (int i : words) {
            int val = corpusToRegionMap[i];
            if (val > -1)
                numericValues.add(val);
        }
        return numericValues;
    }

    private void regenerateCorpusToRegionMap() throws IOException {
        Path mapFile = Paths.get(getPath().toString(), "map.disco");
        IntBuffer ib = FileUtils.readAllInts(mapFile);
        corpusToRegionMap = new int[ib.limit()];
        ib.get(corpusToRegionMap);
        regionToCorpusMap = new int[corpusToRegionMap.length];
        int n = 0;
        for (int i : corpusToRegionMap) {
            if (i >= 0)
                regionToCorpusMap[i] = n;
            n++;
        }
    }

    private List<Integer> getIndexValues(Collection<IndexEntry> indexEntries) {
        List<Integer> allIndexValues = new ArrayList<Integer>();
        for (IndexEntry ie : indexEntries) {
            allIndexValues.addAll(ie.getIndexValuesAsList());
        }
        return allIndexValues;
    }

    private List<int[]> getContexts(List<Integer> indexValues, int left, int right) throws IOException {
        int first = Integer.MAX_VALUE;
        int last = 0;
        for (int i : indexValues) {
            first = (i - left < first) ? i - left : first;
            last = (i + 1 + right > last) ? i + 1 + right : last;
            // this loop calculates the first and last values needed in the file so the whole file doesn't need to be read
        }
        Path dataFile = Paths.get(getPath().toString(), "data.disco");
        long a = System.currentTimeMillis();
        //IntBuffer ib = FileUtils.readInts(dataFile, first, last - first);
        IntBuffer ib = FileUtils.readAllInts(dataFile);
        long b = System.currentTimeMillis();
        LOG.trace("Read " + dataFile.toString() + " in " + (b - a) + "ms");
        List<int[]> contexts = new ArrayList<int[]>();
        for (int i : indexValues) {
            int[] context = new int[left + right + 1];
            int n = i - left;
            for (int j = 0; j < context.length; j++) {
                try {
                    context[j] = regionToCorpusMap[ib.get(n++)];
                } catch (IndexOutOfBoundsException e) {
                    context[j] = -1;
                }
            }
            contexts.add(context);
        }
        long c = System.currentTimeMillis();
        LOG.trace("Processed " + dataFile.toString() + " in " + (c - b) + "ms");
        return contexts;
    }
}
