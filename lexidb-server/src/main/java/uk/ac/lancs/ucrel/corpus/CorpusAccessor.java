package uk.ac.lancs.ucrel.corpus;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
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
        if (!accessors.containsKey(dataPath.toString())) {
            accessors.put(dataPath.toString(), new CorpusAccessor(dataPath));
            accessors.get(dataPath.toString()).buildIndexes();
        }
        return accessors.get(dataPath.toString());
    }

    public static void invalidate(Path dataPath) {
        if (accessors.containsKey(dataPath.toString()))
            accessors.remove(dataPath.toString());
    }

    private void buildIndexes() {
        d.loadIndexTrees();
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

    public Map<Integer, Integer> list(DictionaryEntry word) {
        List<DictionaryEntry> words = new ArrayList<>();
        words.add(word);
        return list(words);
    }

    public Map<Integer, Integer> list(List<DictionaryEntry> words) {
        Map<Integer, Integer> freq = new HashMap<Integer, Integer>();
        List<Integer> numericaValues = getNumericValues(words);
        for (int val : numericaValues) {
            freq.put(val, d.count(val));
        }
        return freq;
    }

    public List<int[]> context(List<DictionaryEntry> words, int leftContext, int rightContext, int limit) throws IOException {
        List<Integer> numericValues = getNumericValues(words);
        Map<Integer, IndexEntry> indexEntries = getIndexPos(numericValues);
        getIndexEntryValues(indexEntries.values());
        return getContexts(indexEntries, leftContext, rightContext, limit);
    }

    public List<DictionaryEntry> getWords(List<String> searchTerms) throws IOException {
        long start = System.currentTimeMillis();
        List<List<DictionaryEntry>> allWords = new ArrayList<List<DictionaryEntry>>();
        for (int i = 0; i < searchTerms.size(); i++) {
            if (searchTerms.get(i) == null)
                continue;
            allWords.addAll(getWords(searchTerms.get(i), i));
        }
        long end = System.currentTimeMillis();
        LOG.trace("Got list of words in " + (end - start) + "ms");
        return intersect(allWords);
    }

    private List<DictionaryEntry> intersect(List<List<DictionaryEntry>> entries) {
        List<DictionaryEntry> initial = removeSmallest(entries);
        while (entries.size() > 0) {
            initial.retainAll(removeSmallest(entries));
        }
        return initial;
    }

    private List<DictionaryEntry> removeSmallest(List<List<DictionaryEntry>> entries) {
        List<DictionaryEntry> smallest = null;
        for (List<DictionaryEntry> lde : entries) {
            if (smallest == null || smallest.size() > lde.size())
                smallest = lde;
        }
        entries.remove(smallest);
        return smallest;
    }

    public List<List<DictionaryEntry>> getWords(String searchTerm, int column) {
        List<List<DictionaryEntry>> words = new ArrayList<List<DictionaryEntry>>();
        if (searchTerm == null)
            return words;
        if (isRegex(searchTerm))
            words.add(regex(searchTerm, column));
        else
            words.add(d.getEntries(searchTerm, column));
        return words;
    }

    public List<DictionaryEntry> regex(String regex, int column) {
        RegExp re = new RegExp(regex);
        Automaton a = re.toAutomaton();
        RunAutomaton ra = new RunAutomaton(a);
        Pattern p = Pattern.compile(regex);
        List<DictionaryEntry> matches = new ArrayList<DictionaryEntry>();
        for (String key : d.getKeys(column)) {
            if (ra.run(key)) {
                matches.addAll(d.getEntries(key, column));
            }
        }
        return matches;
    }

    private boolean isRegex(String s) {
        return s.matches("^.*[^a-zA-Z ].*$");
    }

    private List<Integer> getNumericValues(List<DictionaryEntry> words) {
        List<Integer> numericValues = new ArrayList<Integer>();
        for (DictionaryEntry de : words) {
            numericValues.add(de.getValue());
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
        long a = System.currentTimeMillis();
        Map<Integer, List<Integer>> numericForEachRegion = new HashMap<Integer, List<Integer>>();
        for (int numeric : indexEntries.keySet()) {
            for (int i : indexEntries.get(numeric).getIndexValues()) {
                if (!numericForEachRegion.containsKey(i))
                    numericForEachRegion.put(i, new ArrayList<Integer>());
                numericForEachRegion.get(i).add(numeric);
            }
        }
        long b = System.currentTimeMillis();
        LOG.trace("Calculated numerics for each region in " + (b - a) + "ms");
        List<int[]> contexts = new ArrayList<int[]>();
        int regionsAccessed = 0;
        for (int region : numericForEachRegion.keySet()) {
            long c = System.currentTimeMillis();
            String regionString = regionNameFormatter.format(region);
            RegionAccessor ra = RegionAccessor.getAccessor(Paths.get(getPath().toString(), regionString));
            regionsAccessed++;
            contexts.addAll(ra.contextSearch(numericForEachRegion.get(region), leftContext, rightContext));
            if (limit > 0 && contexts.size() >= limit)
                break;
            long d = System.currentTimeMillis();
            LOG.trace("Got contexts for region " + region + " in " + (d - c) + "ms");
        }
        LOG.debug(regionsAccessed + " regions accessed");
        if (limit > 0 && contexts.size() >= limit)
            contexts = contexts.subList(0, limit);
        return contexts;
    }
}
