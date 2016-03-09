package uk.ac.lancs.ucrel.region;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RegionAccessor {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);
    private static final int BUFFER_SIZE = 1024 * 256;
    private static final int context = 5;

    private Path regionPath;
    private Map<String, Integer> dict;
    private List<String> map;
    int numericValue, indexPos, count;
    List<Integer> indexEntries;
    List<List<Integer>> concLines;

    public RegionAccessor(Path regionPath){
        this.regionPath = regionPath;
    }

    public void search(String word) throws IOException {
        long start = System.currentTimeMillis();
        regenerateDict();
        long a = System.currentTimeMillis();
        numericValue = dict.get(word);
        long b = System.currentTimeMillis();
        getIndexPos();
        long c = System.currentTimeMillis();
        getIndexEntries();
        long d = System.currentTimeMillis();
        getConcordanceLines();
        long end = System.currentTimeMillis();
        LOG.info("Search \"" + word + "\" completed in " + (end - start) + "ms (" + (a-start) + "ms regenDict, " + (b-a) + "ms getNumericVal, " + (c-b) + "ms getIndexPos, " + (d-c) + "ms getIndexEnts, " + (end - d) + "ms getConcLines)");
        printLines();
    }

    private void regenerateDict() throws IOException {
        dict = new HashMap<String, Integer>();
        map = new ArrayList<String>();
        List<String> words = Files.readAllLines(Paths.get(regionPath.toString(), "dict.disco"), StandardCharsets.UTF_8);
        int i = 0;
        for(String word : words){
            map.add(word);
            dict.put(word, i++);
        }
    }

    private void getIndexPos() throws IOException {
        Path indexPosFile = Paths.get(regionPath.toString(), "idx_pos.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexPosFile), BUFFER_SIZE));
        int bytesToSkip = numericValue * 4;
        dis.skipBytes(bytesToSkip);
        indexPos = dis.readInt();
        count = dis.readInt() - indexPos;
        dis.close();
    }

    private void getIndexEntries() throws IOException {
        Path indexEntFile = Paths.get(regionPath.toString(), "idx_ent.disco");
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
        Path dataFile = Paths.get(regionPath.toString(), "data.disco");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(dataFile), BUFFER_SIZE));
        concLines = new ArrayList<List<Integer>>();
        int currentPos = 0;
        for(int pos : indexEntries){
            int wordsToSkip = (pos - currentPos) - context;
            int bytesToSkip = wordsToSkip * 4;
            dis.skipBytes(bytesToSkip);
            currentPos += wordsToSkip;
            List<Integer> line = new ArrayList<Integer>();
            for(int i = 0; i < ((context * 2) + 1); i++){
                line.add(dis.readInt());
                currentPos++;
            }
            concLines.add(line);
        }
        dis.close();
    }

    private void printLines(){
        for(List<Integer> line : concLines){
            System.out.println(getLine(line));
        }
    }

    private String getLine(List<Integer> line){
        StringBuilder sb = new StringBuilder();
        for(int i : line){
            sb.append(map.get(i)).append(" ");
        }
        return sb.toString();
    }
}
