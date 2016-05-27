package uk.ac.lancs.ucrel.dict;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Holds a set of string values and their associated numeric values.
 */
public class Dictionary {

    private Map<String, DictionaryEntry> stringToEntry = new HashMap<String, DictionaryEntry>();
    private DictionaryEntry[] valueToEntry;
    private boolean finalised = false;

    /**
     * Adds a string to the dictionary and returns a numeric value for the string.
     * @param s
     * @return
     */
    public int put(String s) {
        if(finalised)
            throwRuntimeException();
        if (!stringToEntry.containsKey(s))
            stringToEntry.put(s, new DictionaryEntry(s, stringToEntry.size()));
        DictionaryEntry de = stringToEntry.get(s);
        de.increment();
        return de.getValue();
    }

    public int putMany(String s, int n){
        put(s);
        DictionaryEntry de = stringToEntry.get(s);
        de.addToCount(n - 1);
        return de.getValue();
    }

    private void put(String s, int count){
        stringToEntry.put(s, new DictionaryEntry(s, stringToEntry.size(), count));
    }

    /**
     * Puts a collection of strings into the dictionary.
     * @param cs
     */
    public void putAll(Collection<String> cs){
        for(String s : cs){
            put(s);
        }
    }

    private void finalise() {
        valueToEntry = new DictionaryEntry[stringToEntry.size()];
        for (String s : stringToEntry.keySet()) {
            valueToEntry[stringToEntry.get(s).getValue()] = stringToEntry.get(s);
        }
        finalised = true;
    }

    /**
     * Return the numeric value for the string s. Return -1 if the dictionary does not contain the string s.
     * @param s
     * @return
     */
    public int get(String s){
        if(!stringToEntry.containsKey(s))
            return -1;
        return stringToEntry.get(s).getValue();
    }

    /**
     * Returns the string value for the numeric value i.
     * @param i
     * @return
     */
    public String get(int i) {
        if (!finalised)
            finalise();
        return valueToEntry[i].getWord();
    }

    public int count(String s){
        return stringToEntry.get(s).getCount();
    }

    public int count(int i){
        if(!finalised)
            finalise();
        return valueToEntry[i].getCount();
    }

    /**
     * The number of entries in the dictionary.
     * @return
     */
    public int size() {
        return stringToEntry.size();
    }

    /**
     * Maps one dictionary onto another. The returned array will be the size of dictionary a and the index
     * for the string a will contain it's value in b. If a contians a string not present in b then the index will
     * contain -1.
     * @param a
     * @param b
     * @return
     */
    public static int[] map(Dictionary a, Dictionary b) {
        int[] map = new int[a.size()];
        for(String s : a.stringToEntry.keySet()){
            map[a.get(s)] = b.get(s);
        }
        return map;
    }

    /**
     * Generates a new dictionary that contains all strings in d but the values are lexically sorted.
     * @param d
     * @return
     */
    public static Dictionary sort(Dictionary d){
        Dictionary sorted = new Dictionary();
        Set<String> sortedKeys = new TreeSet<String>(d.stringToEntry.keySet());
        for(String s : sortedKeys){
            sorted.put(s, d.count(s));
        }
        return sorted;
    }

    /**
     * Generates a dictionary from the file specified by p.
     * @param p
     * @return
     */
    public static Dictionary load(Path p){
        Dictionary d = new Dictionary();
        try {
            List<String> words = Files.readAllLines(p, StandardCharsets.UTF_8);
            for(String s : words){
                String[] bits = s.split(" ");
                String word = bits[0];
                int count = Integer.parseInt(bits[1]);
                d.put(word, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return d;
    }

    public void save(Path p){
        List<String> lines = new ArrayList<String>();
        for(int i = 0; i < size(); i++){
            lines.add(get(i) + " " + count(i));
        }
        try {
            Files.write(p, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void throwRuntimeException(){
        throw new RuntimeException("Dictionary already finalised!");
    }

    /**
     * Returns a list contain all the entries in the dictionary as strings.
     * @return
     */
    public List<String> getEntries(){
        List<String> entries = new ArrayList<String>(stringToEntry.keySet());
        Collections.sort(entries);
        return entries;
    }
}
