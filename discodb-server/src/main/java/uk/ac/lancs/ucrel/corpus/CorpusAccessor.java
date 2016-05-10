package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.region.RegionAccessor;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorpusAccessor {

    private static final Logger LOG = LogManager.getLogger(CorpusAccessor.class);
    private static final int BUFFER_SIZE = 1024 * 256;

    private Path corpusPath;
    private Map<String, Integer> dict;
    private List<String> wordList;
    private int count, limit, regionsAccessed, context, totalWordCount, regexMatches;
    private DecimalFormat regionNameFormatter;

    public CorpusAccessor(Path corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        regionNameFormatter = new DecimalFormat("0000");
        generateDictionary();
    }

    public int getWordCount(){
        return totalWordCount;
    }

    public int getWordTypeCount(){
        return wordList.size();
    }

    public List<int[]> regex(String regex, int context, int limit) throws IOException {
        Pattern p = Pattern.compile(regex);
        List<int[]> lines = new ArrayList<int[]>();
        regexMatches = 0;
        for(String word : wordList){
            if(p.matcher(word).matches()) {
                lines.addAll(search(word, context, limit));
                regexMatches++;
            }
        }
        return lines;
    }

    public int getRegexMatches(){
        return regexMatches;
    }

    public List<int[]> search(String w, int context, int limit) throws IOException {
        long start = System.currentTimeMillis();
        this.limit = limit;
        this.context = context;
        int numericValue = dict.get(w);
        int indexPos = getIndexPos(numericValue);
        List<Integer> indexEntries = getIndexEntries(indexPos);
        List<int[]> lines = getConcordanceLines(numericValue, indexEntries);
        long end = System.currentTimeMillis();
        LOG.info("Search for \"" + w + "\" in " + (end - start) + "ms from " + regionsAccessed + " regions");
        return lines;
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
        List<String> words = Files.readAllLines(Paths.get(corpusPath.toString(), "dict.disco"), StandardCharsets.UTF_8);
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

    private int getIndexPos(int numericValue) throws IOException {
        Path indexPosFile = Paths.get(corpusPath.toString(), "idx_pos.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexPosFile), BUFFER_SIZE));
        int bytesToSkip = numericValue * 4;
        dis.skipBytes(bytesToSkip);
        int indexPos = dis.readInt();
        count = dis.readInt() - indexPos;
        dis.close();
        return indexPos;
    }

    private List<Integer> getIndexEntries(int indexPos) throws IOException{
        Path indexEntFile = Paths.get(corpusPath.toString(), "idx_ent.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexEntFile), BUFFER_SIZE));
        int bytesToSkip = indexPos * 4;
        dis.skipBytes(bytesToSkip);
        List<Integer> indexEntries = new ArrayList<Integer>();
        for(int i = 0; i < count; i++){
            indexEntries.add(dis.readInt());
        }
        dis.close();
        return indexEntries;
    }

    private List<int[]> getConcordanceLines(int numericValue, List<Integer> indexEntries) throws IOException {
        List<int[]> lines = new ArrayList<int[]>();
        regionsAccessed = 0;
        for(int i : indexEntries){
            String region = regionNameFormatter.format(i);
            RegionAccessor ra = new RegionAccessor(Paths.get(corpusPath.toString(), region));
            lines.addAll(ra.search(numericValue, context, limit));
            regionsAccessed++;
            if(lines.size() >= limit && limit > 0)
                break;
        }
        return lines;
    }
}
