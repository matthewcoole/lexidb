package uk.ac.lancs.ucrel.dict;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static uk.ac.lancs.ucrel.dict.DictionaryEntry.cleanTsv;

/**
 * Holds a set of string values and their associated numeric values.
 */
public class Dictionary {

    private static Logger LOG = Logger.getLogger(Dictionary.class);
    private Map<String, DictionaryEntry> stringToEntry = new HashMap<String, DictionaryEntry>();
    private Map<DictionaryEntry, Integer> entryToValue = new HashMap<DictionaryEntry, Integer>();
    private Map<String, List<String>> wordToString = new HashMap<String, List<String>>();
    private DictionaryEntry[] valueToEntry;
    private List<Map<String, List<DictionaryEntry>>> indexTrees = new ArrayList<Map<String, List<DictionaryEntry>>>();
    private boolean finalised = false;

    /**
     * Maps one dictionary onto another. The returned array will be the size of dictionary a and the index
     * for the string a will contain it's value in b. If a contians a string not present in b then the index will
     * contain -1.
     *
     * @param a
     * @param b
     * @return
     */
    public static int[] map(Dictionary a, Dictionary b) {
        int[] map = new int[a.size()];
        for (String s : a.stringToEntry.keySet()) {
            map[a.get(s)] = b.get(s);
        }
        return map;
    }

    /**
     * Generates a new dictionary that contains all strings in d but the values are lexically sorted.
     *
     * @param d
     * @return
     */
    public static Dictionary sort(Dictionary d) {
        Dictionary sorted = new Dictionary();
        List<String> sortedKeys = new ArrayList<String>(d.stringToEntry.keySet());
        Collections.sort(sortedKeys, String.CASE_INSENSITIVE_ORDER);

        //Set<String> sortedKeys = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        //sortedKeys.addAll(d.stringToEntry.keySet());
        //d.stringToEntry.keySet().removeAll(sortedKeys);
        for (String s : sortedKeys) {
            sorted.put(s, d.count(s));
        }
        return sorted;
    }

    /**
     * Generates a dictionary from the file specified by p.
     *
     * @param p
     * @return
     */
    public static Dictionary load(Path p) {
        long start = System.currentTimeMillis();
        Dictionary d = new Dictionary();
        try {
            long a = System.currentTimeMillis();
            List<String> words = Files.readAllLines(p, StandardCharsets.UTF_8);
            long b = System.currentTimeMillis();
            LOG.trace("Loading dictionary file took " + (b - a) + "ms");
            for (String s : words) {
                String[] bits = s.split("\t");
                String word = s.substring(0, s.lastIndexOf("\t"));
                int count = Integer.parseInt(bits[bits.length - 1]);
                d.put(word, count);
            }
            long c = System.currentTimeMillis();
            LOG.trace("Processing lines of dictionary took " + (c - b) + "ms");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        long end = System.currentTimeMillis();
        LOG.trace("Dicitonary loaded in " + (end - start) + "ms");
        return d;
    }

    /**
     * Generates indexes for each column in the dictionary.
     */
    public void loadIndexTrees() {
        long start = System.currentTimeMillis();
        int n = stringToEntry.values().size() / 5;
        Iterator<DictionaryEntry> it = stringToEntry.values().iterator();
        for (int i = 0; i < n; i++) {
            it.next();
        }
        int indexCount = it.next().getTags().size() + 1;
        for (int i = 0; i < indexCount; i++) {
            indexTrees.add(new HashMap<String, List<DictionaryEntry>>());
        }
        for (DictionaryEntry de : stringToEntry.values()) {
            Map<String, List<DictionaryEntry>> wordTree = indexTrees.get(0);
            if (!wordTree.containsKey(de.getWord()))
                wordTree.put(de.getWord(), new ArrayList<DictionaryEntry>());
            wordTree.get(de.getWord()).add(de);

            int i = 1;
            for (String tag : de.getTags()) {
                Map<String, List<DictionaryEntry>> tree = indexTrees.get(i);
                if (!tree.containsKey(tag))
                    tree.put(tag, new ArrayList<DictionaryEntry>());
                tree.get(tag).add(de);
                i++;
            }
        }
        long end = System.currentTimeMillis();
        LOG.trace("Loaded index trees in " + (end - start) + "ms");
    }

    /**
     * Adds a string to the dictionary and returns a numeric value for the string.
     *
     * @param s
     * @return
     */
    public int put(String s) {
        if (finalised)
            throwRuntimeException();
        DictionaryEntry de = new DictionaryEntry(s, stringToEntry.size());
        if (!stringToEntry.containsKey(de.toString())) {
            stringToEntry.put(de.toString(), de);
            if (!wordToString.containsKey(de.getWord()))
                wordToString.put(de.getWord(), new ArrayList<String>());
            wordToString.get(de.getWord()).add(de.toString());
        }
        de = stringToEntry.get(de.toString());
        de.increment();
        return de.getValue();
    }

    public int putMany(String s, int n) {
        s = cleanTsv(s);
        put(s);
        DictionaryEntry de = stringToEntry.get(s);
        de.addToCount(n - 1);
        return de.getValue();
    }

    private void put(String s, int count) {
        DictionaryEntry de = new DictionaryEntry(s, stringToEntry.size(), count);
        stringToEntry.put(de.toString(), de);
        if (!wordToString.containsKey(de.getWord()))
            wordToString.put(de.getWord(), new ArrayList<String>());
        wordToString.get(de.getWord()).add(s);
    }

    /**
     * Puts a collection of strings into the dictionary.
     *
     * @param cs
     */
    public void putAll(Collection<String> cs) {
        for (String s : cs) {
            put(s);
        }
    }

    private void finalise() {
        valueToEntry = new DictionaryEntry[stringToEntry.size()];
        for (String s : stringToEntry.keySet()) {
            int val = stringToEntry.get(s).getValue();
            valueToEntry[val] = stringToEntry.get(s);
            entryToValue.put(stringToEntry.get(s), val);
        }
        finalised = true;
    }

    /**
     * Return the numeric value for the string s. Return -1 if the dictionary does not contain the string s.
     *
     * @param s
     * @return
     */
    public int get(String s) {
        if (!stringToEntry.containsKey(s))
            return -1;
        return stringToEntry.get(s).getValue();
    }

    public Integer[] get(String s, int column) {
        List<DictionaryEntry> results = indexTrees.get(column).get(s);
        List<Integer> list = new ArrayList<Integer>();
        for (DictionaryEntry de : results) {
            list.add(de.getValue());
        }
        Integer[] nums = list.toArray(new Integer[0]);
        Arrays.sort(nums);
        return nums;
    }

    public List<Integer> getWords(String s) {
        List<Integer> li = new ArrayList<Integer>();
        if (wordToString.containsKey(s)) {
            for (String word : wordToString.get(s)) {
                li.add(get(word));
            }
        }
        return li;
    }

    public List<Integer> getNumericValues(DictionaryEntry de) {
        List<Integer> li = new ArrayList<Integer>();
        if (entryToValue.containsKey(de)) {
            li.add(entryToValue.get(de));
        }
        return li;
    }

    /**
     * Returns the string value for the numeric value i.
     *
     * @param i
     * @return
     */
    public String get(int i) {
        if (!finalised)
            finalise();
        if (i == -1)
            return "";
        return valueToEntry[i].toString();
    }

    public int count(String s) {
        return stringToEntry.get(s).getCount();
    }

    public int count(int i) {
        if (!finalised)
            finalise();
        return valueToEntry[i].getCount();
    }

    /**
     * The number of entries in the dictionary.
     *
     * @return
     */
    public int size() {
        return stringToEntry.size();
    }

    public void save(Path p) {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < size(); i++) {
            lines.add(get(i) + "\t" + count(i));
        }
        try {
            Files.write(p, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void throwRuntimeException() {
        throw new RuntimeException("Dictionary already finalised!");
    }

    /**
     * Returns a list contain all the entries in the dictionary as strings.
     *
     * @return
     */
    public List<String> getEntries() {
        List<String> entries = new ArrayList<String>(stringToEntry.keySet());
        Collections.sort(entries, String.CASE_INSENSITIVE_ORDER);
        return entries;
    }

    public List<String> getKeys(int column) {
        List<String> entries = new ArrayList<String>();
        entries.addAll(indexTrees.get(column).keySet());
        return entries;
    }

    public List<DictionaryEntry> getEntries(String key, int column) {
        if (column < indexTrees.size() && indexTrees.get(column).containsKey(key))
            return indexTrees.get(column).get(key);
        else
            return new ArrayList<DictionaryEntry>();
    }
}
