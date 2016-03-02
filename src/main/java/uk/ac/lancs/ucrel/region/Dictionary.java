package uk.ac.lancs.ucrel.region;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Dictionary {

    private Path regionPath, dictPath;
    private SortedMap<String, Integer> lookup;
    private int[] initToFinalMap;
    private String[] valToStringMap;

    public Dictionary(Path regionPath) throws IOException {
        this.regionPath = regionPath;
        dictPath = Paths.get(regionPath.toString(), "dict.disco");
        Files.deleteIfExists(dictPath);
        Files.createFile(dictPath);
        lookup = new TreeMap<String, Integer>();
    }

    public boolean contains(String w){
        return lookup.containsKey(w);
    }

    public void add(String w){
        lookup.put(w, lookup.size());
    }

    public Integer get(String w){
        return lookup.get(w);
    }

    public void finalize(){
        initToFinalMap = new int[lookup.size()];
        valToStringMap = new String[lookup.size()];
        int newVal = 0;
        for(String s : lookup.keySet()){
            valToStringMap[newVal] = s;
            int currentVal = lookup.get(s);
            lookup.put(s, newVal);
            initToFinalMap[currentVal] = newVal++;
        }
        System.out.println(lookup);
    }
}
