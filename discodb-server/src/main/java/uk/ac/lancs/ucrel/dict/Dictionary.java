package uk.ac.lancs.ucrel.dict;

import java.util.*;

/**
 * Holds a set of string values and their associated numeric values.
 */
public class Dictionary {

    private Map<String, Integer> stringToValue = new HashMap<String, Integer>();
    private String[] valueToString;
    private boolean finalised = false;

    /**
     * Adds a string to the dictionary and returns a numeric value for the string.
     * @param s
     * @return
     */
    public int put(String s) {
        if(finalised)
            throwRuntimeException();
        if (!stringToValue.containsKey(s))
            stringToValue.put(s, stringToValue.size());
        return stringToValue.get(s);
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
        valueToString = new String[stringToValue.size()];
        for (String s : stringToValue.keySet()) {
            valueToString[stringToValue.get(s)] = s;
        }
        finalised = true;
    }

    /**
     * Return the numeric value for the string s. Return -1 if the dictionary does not contain the string s.
     * @param s
     * @return
     */
    public int get(String s){
        if(!stringToValue.containsKey(s))
            return -1;
        return stringToValue.get(s);
    }

    /**
     * Returns the string value for the numeric value i.
     * @param i
     * @return
     */
    public String get(int i) {
        if (!finalised)
            finalise();
        return valueToString[i];
    }

    /**
     * The number of entries in the dictionary.
     * @return
     */
    public int size() {
        return stringToValue.size();
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
        for(String s : a.stringToValue.keySet()){
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
        Set<String> sortedKeys = new TreeSet<String>(d.stringToValue.keySet());
        sorted.putAll(sortedKeys);
        return sorted;
    }

    private void throwRuntimeException(){
        throw new RuntimeException("Dictionary already finalised!");
    }

    /**
     * Returns a list contain all the entries in the dictionary as strings.
     * @return
     */
    public List<String> getEntries(){
        return new ArrayList<>(stringToValue.keySet());
    }
}
