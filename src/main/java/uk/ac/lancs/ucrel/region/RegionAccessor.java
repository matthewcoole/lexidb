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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class RegionAccessor {

    private static final Logger LOG = LogManager.getLogger(RegionBuilder.class);
    private static final int BUFFER_SIZE = 1024 * 256;
    private static final int context = 5;

    private Path regionPath;
    private SortedMap<String, Integer> dict;
    private String[] map;
    int numericValue, indexPos, count;
    List<Integer> indexEntries;
    List<List<Integer>> concLines;

    public RegionAccessor(Path regionPath){
        this.regionPath = regionPath;
    }

    public void search(String word) throws IOException {
        long start = System.currentTimeMillis();
        regenerateDict();
        numericValue = dict.get(word);
        getIndexPos();
        getIndexEntries();
        getConcordanceLines();
        long end = System.currentTimeMillis();
        LOG.info("Search \"" + word + "\" completed in " + (end - start) + "ms");
        printLines();
    }

    private void regenerateDict() throws IOException {
        dict = new TreeMap<String, Integer>();
        List<String> words = Files.readAllLines(Paths.get(regionPath.toString(), "dict.disco"), StandardCharsets.UTF_8);
        int i = 0;
        for(String word : words){
            dict.put(word, i++);
        }
        map = new String[dict.size()];
        for(String word : dict.keySet()){
            map[dict.get(word)] = word;
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
            //if(concLines.size() >= 20)
                //break;
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
            sb.append(map[i]).append(" ");
        }
        return sb.toString();
    }
}
