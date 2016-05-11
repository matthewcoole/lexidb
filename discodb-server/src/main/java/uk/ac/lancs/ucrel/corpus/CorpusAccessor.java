package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.file.system.FileUtils;
import uk.ac.lancs.ucrel.index.IndexEntry;
import uk.ac.lancs.ucrel.region.RegionAccessor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorpusAccessor extends Accessor {

    private static final Logger LOG = LogManager.getLogger(CorpusAccessor.class);

    private Map<String, Integer> dict;
    private List<String> wordList;
    private int limit, context, totalWordCount;
    private DecimalFormat regionNameFormatter;

    public CorpusAccessor(Path corpusPath) throws IOException {
        setPath(corpusPath);
        regionNameFormatter = new DecimalFormat("0000");
        generateDictionary();
    }

    public int getWordCount(){
        return totalWordCount;
    }

    public int getWordTypeCount(){
        return wordList.size();
    }

    public List<String> regex(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        List<String> matches = new ArrayList<String>();
        for(String word : wordList){
            if(p.matcher(word).matches()) {
                matches.add(word);
            }
        }
        return matches;
    }

    public List<int[]> search(String w, int context, int limit) throws IOException {
        List<String> words = new ArrayList<String>();
        words.add(w);
        return search(words, context, limit);
    }

    public List<int[]> search(List<String> words, int context, int limit) throws IOException {
        this.limit = limit;
        this.context = context;
        List<Integer> numericValues = getNumericValues(words);
        List<IndexEntry> indexEntries = getIndexPositions(numericValues);
        getIndexEntryValues(indexEntries);
        List<int[]> lines = getConcordanceLines(numericValues, indexEntries);
        return lines;
    }

    private void getIndexEntryValues(List<IndexEntry> indexEntries) throws IOException {
        for(IndexEntry ie : indexEntries){
            getIndexEntryValues(ie);
        }
    }

    private List<IndexEntry> getIndexPositions(List<Integer> numericValues) throws IOException {
        List<IndexEntry> indexEntries = new ArrayList<IndexEntry>();
        for(int i : numericValues){
            indexEntries.add(getIndexPos(i));
        }
        return indexEntries;
    }

    private List<Integer> getNumericValues(List<String> words) {
        List<Integer> numericValues = new ArrayList<Integer>();
        for(String w : words){
            numericValues.add(dict.get(w));
        }
        return numericValues;
    }

    public List<String> getLinesAsString(List<int[]> lines) {
        List<String> finalLines = new ArrayList<String>();
        for(int[] line : lines){
            finalLines.add(getLineAsString(line));
        }
        return finalLines;
    }

    public String getLineAsString(int[] line){
        StringBuilder sb = new StringBuilder();
        for(Integer i : line){
            sb.append(wordList.get(i)).append(' ');
        }
        return sb.toString().trim();
    }

    private void generateDictionary () throws IOException {
        List<String> words = Files.readAllLines(Paths.get(getPath().toString(), "dict.disco"), StandardCharsets.UTF_8);
        dict = new HashMap<String, Integer>();
        wordList = new ArrayList<String>();
        int i = 0;
        for(String s : words){
            String[] items = s.split(" ");
            String word = items[0];
            totalWordCount += Integer.parseInt(items[1]);
            dict.put(word, i++);
            wordList.add(word);
        }
    }

    private List<int[]> getConcordanceLines(List<Integer> numericValues, List<IndexEntry> indexEntries) throws IOException {
        List<int[]> lines = new ArrayList<int[]>();
        int regionsAccessed = 0;
        Set<Integer> regions = new HashSet<Integer>();
        for(IndexEntry ie : indexEntries){
            for(int i : ie.getIndexValues()){
                regions.add(i);
            }
        }
        for(int i : regions){
            String region = regionNameFormatter.format(i);
            RegionAccessor ra = new RegionAccessor(Paths.get(getPath().toString(), region));
            lines.addAll(ra.search(numericValues, context, limit));
            regionsAccessed++;
            if(lines.size() >= limit && limit > 0)
                break;
        }
        return lines;
    }
}
