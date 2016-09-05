package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.index.IndexEntry;
import uk.ac.lancs.ucrel.region.RegionAccessor;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CorpusAccessor extends Accessor {

    private static final Logger LOG = LogManager.getLogger(CorpusAccessor.class);

    private static Map<String, CorpusAccessor> accessors = new HashMap<String, CorpusAccessor>();

    private Dictionary d;
    private DecimalFormat regionNameFormatter;

    public CorpusAccessor(Path corpusPath) throws IOException {
        setPath(corpusPath);
        regionNameFormatter = new DecimalFormat("0000");
        d = Dictionary.load(Paths.get(getPath().toString(), "dict.disco"));
    }

    public static CorpusAccessor getAccessor(Path dataPath) throws IOException {
        if (!accessors.containsKey(dataPath.toString()))
            accessors.put(dataPath.toString(), new CorpusAccessor(dataPath));
        return accessors.get(dataPath.toString());
    }

    public static void invalidate(Path dataPath) {
        if (accessors.containsKey(dataPath.toString()))
            accessors.remove(dataPath.toString());
    }

    public int getWordCount() {
        int wordCount = 0;
        for (String s : d.getEntries()) {
            wordCount += d.count(s);
        }
        return wordCount;
    }

    public int getWordTypeCount() {
        return d.size();
    }

    private List<String> regex(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        List<String> matches = new ArrayList<String>();
        for (String word : d.getEntries()) {
            String[] bits = word.split("\t");
            if (p.matcher(bits[0]).matches()) {
                matches.add(bits[0]);
            }
        }
        return matches;
    }

    public Map<Integer, Integer> list(String word) {
        List<String> words = new ArrayList<>();
        words.add(word);
        return list(words);
    }

    public Map<Integer, Integer> list(List<String> words) {
        Map<Integer, Integer> freq = new HashMap<Integer, Integer>();
        List<Integer> numericaValues = getNumericValues(words);
        for (int val : numericaValues) {
            freq.put(val, d.count(val));
        }
        return freq;
    }

    public List<int[]> context(List<String> words, int leftContext, int rightContext, int limit) throws IOException {
        List<Integer> numericValues = getNumericValues(words);
        Map<Integer, IndexEntry> indexEntries = getIndexPositions(numericValues);
        getIndexEntryValues(indexEntries);
        return getContexts(indexEntries, leftContext, rightContext, limit);
    }

    public List<String> getWords(String searchTerm) throws IOException {
        List<String> words = new ArrayList<>();
        if (isRegex(searchTerm))
            words.addAll(regex(searchTerm));
        else
            words.add(searchTerm);
        return words;
    }

    private boolean isRegex(String s) {
        return s.matches("^.*[^a-zA-Z ].*$");
    }

    private void getIndexEntryValues(Map<Integer, IndexEntry> indexEntries) throws IOException {
        for (IndexEntry ie : indexEntries.values()) {
            getIndexEntryValues(ie);
        }
    }

    private Map<Integer, IndexEntry> getIndexPositions(List<Integer> numericValues) throws IOException {
        Map<Integer, IndexEntry> indexEntries = new HashMap<Integer, IndexEntry>();
        for (int i : numericValues) {
            indexEntries.put(i, getIndexPos(i));
        }
        return indexEntries;
    }

    private List<Integer> getNumericValues(List<String> words) {
        List<Integer> numericValues = new ArrayList<Integer>();
        for (String w : words) {
            numericValues.addAll(d.getWords(w));
        }
        return numericValues;
    }

    public Word getWord(int i) {
        String s = d.get(i);
        String[] parts = s.split("\t");
        Word w = new Word(parts[0]);
        for (int n = 1; n < parts.length; n++) {
            w.addTag(parts[n]);
        }
        return w;
    }

    public Kwic getLine(int[] line) {
        Kwic l = new Kwic();
        for (int i : line) {
            l.add(getWord(i));
        }
        return l;
    }

    private List<int[]> getContexts(Map<Integer, IndexEntry> indexEntries, int leftContext, int rightContext, int limit) throws IOException {
        Map<Integer, List<Integer>> numericForEachRegion = new HashMap<Integer, List<Integer>>();
        for (int numeric : indexEntries.keySet()) {
            for (int i : indexEntries.get(numeric).getIndexValues()) {
                if (!numericForEachRegion.containsKey(i))
                    numericForEachRegion.put(i, new ArrayList<Integer>());
                numericForEachRegion.get(i).add(numeric);
            }
        }
        List<int[]> contexts = new ArrayList<int[]>();
        for (int region : numericForEachRegion.keySet()) {
            String regionString = regionNameFormatter.format(region);
            RegionAccessor ra = new RegionAccessor(Paths.get(getPath().toString(), regionString));
            contexts.addAll(ra.contextSearch(numericForEachRegion.get(region), leftContext, rightContext));
            if (limit > 0 && contexts.size() >= limit)
                break;
        }
        if (limit > 0 && contexts.size() >= limit)
            contexts = contexts.subList(0, limit);
        return contexts;
    }
}
