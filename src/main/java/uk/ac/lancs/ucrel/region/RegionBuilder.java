package uk.ac.lancs.ucrel.region;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RegionBuilder {

    private Path regionPath;
    private List<String> words;
    private int[] data;
    private SortedMap<String, Integer> dict;
    private int[] initToFinalMap;
    private List<List<Integer>> index;
    private int typeCount;


    public RegionBuilder(Path regionPath){
        this.regionPath = regionPath;
        words = new ArrayList<String>();
    }

    public void add(List<String> words){
        this.words.addAll(words);
    }

    public void build(){
        init();
        System.out.println(this);
        assignInitNumericValues();
        System.out.println(this);
        generateInitToFinalMap();
        System.out.println(this);
        initIndex();
        System.out.println(this);
        assignFinalNumericValues();
        System.out.println(this);
    }

    private void init(){
        data = new int[words.size()];
        dict = new TreeMap<String, Integer>();
        typeCount = 0;
    }

    private void assignInitNumericValues(){
        int i = 0;
        for(String word : words){
            if(!dict.containsKey(word))
                dict.put(word, typeCount++);
            data[i] = dict.get(word);
            i++;
        }
    }

    private void generateInitToFinalMap(){
        initToFinalMap = new int[dict.size()];
        int i = 0;
        for(String word : dict.keySet()){
            initToFinalMap[dict.get(word)] = i++;
        }
    }

    private void assignFinalNumericValues(){
        for(int i = 0; i < data.length; i++){
            data[i] = initToFinalMap[data[i]];
            addIndexEntry(data[i], i);
        }
    }

    private void initIndex(){
        index = new ArrayList<List<Integer>>();
        for(int n : initToFinalMap){
            index.add(new ArrayList<Integer>());
        }
    }

    private void addIndexEntry(int numericValue, int pos){
        index.get(numericValue).add(pos);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("dict: ");
        sb.append(dict.toString());
        sb.append(", initToFinalMap: {");
        if(initToFinalMap != null){
            for(int n : initToFinalMap){
                sb.append(n).append(", ");
            }
        }
        sb.append("}, data: {");
        if(data != null){
            for(int n : data){
                sb.append(n).append(", ");
            }
        }
        sb.append("}, index: {");
        if(index != null){
            sb.append(index.toString());
        }
        sb.append("}}");
        return sb.toString();
    }

}
