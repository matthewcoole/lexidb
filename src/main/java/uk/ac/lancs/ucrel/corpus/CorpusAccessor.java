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

public class CorpusAccessor {

    private static final Logger LOG = LogManager.getLogger(CorpusAccessor.class);
    private static final int BUFFER_SIZE = 1024 * 256;

    private Path corpusPath;
    private Map<String, Integer> dict;
    private int numericValue, indexPos, count, limit, regionsAccessed;
    private List<Integer> indexEntries;
    private DecimalFormat regionNameFormatter;
    private String word;
    private List<String> concLines;

    public CorpusAccessor(Path corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        regionNameFormatter = new DecimalFormat("0000");
        generateDictionary();
    }

    public void search(String w, int limit, boolean print) throws IOException {
        long start = System.currentTimeMillis();
        this.limit = limit;
        word = w;
        numericValue = dict.get(w);
        getIndexPos();
        getIndexEntries();
        getConcordanceLines();
        long end = System.currentTimeMillis();
        LOG.info("Search for \"" + word + "\" in " + (end - start) + "ms from " + regionsAccessed + " regions");
        if(print)
            printLines();
    }

    private void generateDictionary () throws IOException {
        List<String> words = Files.readAllLines(Paths.get(corpusPath.toString(), "dict.disco"), StandardCharsets.UTF_8);
        dict = new HashMap<String, Integer>();
        int i = 0;
        for(String s : words){
            dict.put(s, i++);
        }
    }

    private void getIndexPos() throws IOException {
        Path indexPosFile = Paths.get(corpusPath.toString(), "idx_pos.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexPosFile), BUFFER_SIZE));
        int bytesToSkip = numericValue * 4;
        dis.skipBytes(bytesToSkip);
        indexPos = dis.readInt();
        count = dis.readInt() - indexPos;
        dis.close();
    }

    private void getIndexEntries() throws IOException{
        Path indexEntFile = Paths.get(corpusPath.toString(), "idx_ent.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexEntFile), BUFFER_SIZE));
        int bytesToSkip = indexPos * 4;
        dis.skipBytes(bytesToSkip);
        indexEntries = new ArrayList<Integer>();
        for(int i = 0; i < count; i++){
            indexEntries.add(dis.readInt());
        }
        dis.close();
    }

    private void getConcordanceLines() throws IOException {
        concLines = new ArrayList<String>();
        regionsAccessed = 0;
        for(int i : indexEntries){
            String region = regionNameFormatter.format(i);
            RegionAccessor ra = new RegionAccessor(Paths.get(corpusPath.toString(), region));

            concLines.addAll(ra.search(word, limit));
            ra.search(numericValue, limit);

            regionsAccessed++;
            if(concLines.size() >= limit)
                break;
        }
    }

    private void printLines(){
        for(String s : concLines){
            System.out.println(s);
        }
    }
}
