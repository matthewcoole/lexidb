package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.access.Accessor;
import uk.ac.lancs.ucrel.dict.*;
import uk.ac.lancs.ucrel.dict.Dictionary;
import uk.ac.lancs.ucrel.index.IndexEntry;
import uk.ac.lancs.ucrel.region.RegionAccessor;
import uk.ac.lancs.ucrel.result.FullKwicResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Pattern;

public class CorpusAccessor extends Accessor {

    private static final Logger LOG = LogManager.getLogger(CorpusAccessor.class);

    private static Map<String, CorpusAccessor> accessors = new HashMap<String, CorpusAccessor>();

    private Dictionary d;
    private int limit, context;
    private DecimalFormat regionNameFormatter;

    public CorpusAccessor(Path corpusPath) throws IOException {
        setPath(corpusPath);
        regionNameFormatter = new DecimalFormat("0000");
        d = Dictionary.load(Paths.get(getPath().toString(), "dict.disco"));
    }

    public static CorpusAccessor getAccessor(Path dataPath) throws IOException {
        if(!accessors.containsKey(dataPath.toString()))
            accessors.put(dataPath.toString(), new CorpusAccessor(dataPath));
        return accessors.get(dataPath.toString());
    }

    public static void invalidate(Path dataPath){
        if(accessors.containsKey(dataPath.toString()))
            accessors.remove(dataPath.toString());
    }

    public int getWordCount(){
        int wordCount = 0;
        for(String s : d.getEntries()){
            wordCount += d.count(s);
        }
        return wordCount;
    }

    public int getWordTypeCount(){
        return d.size();
    }

    public List<String> regex(String regex) throws IOException {
        Pattern p = Pattern.compile(regex);
        List<String> matches = new ArrayList<String>();
        for(String word : d.getEntries()){
            if(p.matcher(word).matches()) {
                matches.add(word);
            }
        }
        return matches;
    }

    public Map<Integer, Integer> list(String word){
        List<String> words = new ArrayList<>();
        words.add(word);
        return list(words);
    }

    public Map<Integer, Integer> list(List<String> words){
        Map<Integer, Integer> freq = new HashMap<Integer, Integer>();
        List<Integer> numericaValues = getNumericValues(words);
        for(int val : numericaValues){
            freq.put(val, d.count(val));
        }
        return freq;
    }

    public FullKwicResult kwic(String searchTerm, int context, int limit) throws IOException {
        this.limit = limit;
        this.context = context;

        List<String> words = new ArrayList<>();
        if(isRegex(searchTerm))
            words.addAll(regex(searchTerm));
        else
            words.add(searchTerm);

        List<Integer> numericValues = getNumericValues(words);
        List<IndexEntry> indexEntries = getIndexPositions(numericValues);
        getIndexEntryValues(indexEntries);
        List<int[]> lines = getConcordanceLines(numericValues, indexEntries);
        return new FullKwicResult(searchTerm, words.size(), context, lines);
    }

    private boolean isRegex(String s) {
        return s.matches("^.*[^a-zA-Z ].*$");
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
            numericValues.add(d.get(w));
        }
        return numericValues;
    }

    public String getLineAsString(int[] line){
        StringBuilder sb = new StringBuilder();
        for(Integer i : line){
            sb.append(d.get(i)).append(' ');
        }
        return sb.toString().trim();
    }

    private List<int[]> getConcordanceLines(List<Integer> numericValues, List<IndexEntry> indexEntries) throws IOException {
        List<int[]> lines = new ArrayList<int[]>();
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
            if(lines.size() >= limit && limit > 0)
                break;
        }
        return lines;
    }
}
