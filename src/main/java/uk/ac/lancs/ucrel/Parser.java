package uk.ac.lancs.ucrel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Matt on 17/02/2016.
 */
public class Parser {

    private SortedMap<String, Integer> dict = new TreeMap<String, Integer>();
    private int tokenCount, typeCount, dataFileNum, tempFile = 0;
    private int dataFileLimit = 1000000;
    private int bufferSize = 1024 * 2;
    private int[] tempMap;
    private String[] map;
    private Path dataPath, dataFile;
    private DataOutputStream tempData, data;

    public Parser(Path dataPath) throws IOException {
        this.dataPath = dataPath;
        Files.createDirectories(dataPath);
    }

    public void parseDir(Path dir) throws IOException {
        parseInputFiles(dir);
        createMapping();
        parseTempData();
        readFile();
        System.out.println("tokens: " + tokenCount + " type: " + typeCount);
    }

    private void readFile() throws IOException {
        DataInputStream dat = new DataInputStream(Files.newInputStream(dataFile));
        for(int i = 0; i < 10; i++){
            int n = dat.readInt();
            System.out.println(map[n]);
        }
    }

    private void nextTempFile() throws IOException {
        Path temp = Paths.get(dataPath.toString(), tempFile++ + ".tmp");
        Files.deleteIfExists(temp);
        Files.createFile(temp);
        tempData = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(temp), bufferSize));
    }

    private void nextDataFile() throws IOException {
        dataFile = Paths.get(dataPath.toString(), this.dataFileNum++ + ".discordb");
        Files.deleteIfExists(dataFile);
        Files.createFile(dataFile);
        data = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(dataFile), bufferSize));
    }

    private void createMapping(){
        tempMap = new int[dict.size()];
        map = new String[dict.size()];
        int newVal = 0;
        for(String s : dict.keySet()){
            map[newVal] = s;
            tempMap[dict.get(s)] = newVal++;
        }
    }

    private void parseInputFiles(Path dir) throws IOException {
        nextTempFile();
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                List<String> input = Files.readAllLines(file, StandardCharsets.UTF_8);
                for(String s : input){
                    parseLine(s);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        tempData.close();
    }

    private void parseTempData() throws IOException {
        nextDataFile();
        Files.walkFileTree(dataPath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                DataInputStream tmp = new DataInputStream(new BufferedInputStream(Files.newInputStream(file), bufferSize));
                while(tmp.available() > 0){
                    int i = tmp.readInt();
                    data.writeInt(tempMap[i]);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        tempData.close();
    }

    private void parseLine(String line) throws IOException {
        String[] words = line.split("\\s+");
        for(String w : words){
            tokenCount++;
            if(!dict.containsKey(w)){
                dict.put(w, typeCount++);
            }
            tempData.writeInt(dict.get(w));
        }
    }
}
