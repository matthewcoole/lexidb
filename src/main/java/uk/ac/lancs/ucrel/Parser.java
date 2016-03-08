package uk.ac.lancs.ucrel;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Parser {

    private SortedMap<String, Integer> dict = new TreeMap<String, Integer>();
    private String[] finalDict;
    private int tokenCount, typeCount, dataFileNum, tempFile = 0;
    private int dataFileLimit = 1000000;
    private int bufferSize = 1024 * 2;
    private int[] tempMap;
    private String[] map;
    private List<List<Integer>> index;
    private Path dataPath, dataFile;
    private DataOutputStream tempData, data;

    public Parser(Path dataPath) throws IOException {
        this.dataPath = dataPath;
        Files.createDirectories(dataPath);
    }

    public void parseDir(Path dir) throws IOException {
        long start = System.currentTimeMillis();
        parseInputFiles(dir);
        createMapping();
        parseTempData();
        writeIndex();
        long end = System.currentTimeMillis();
        System.out.println("Parsed " + tokenCount + " tokens with " + typeCount + " types in " + (end - start) + "ms.");
    }

    private void writeIndex() throws IOException {
        for(String s : dict.keySet()){
            StringBuilder sb = new StringBuilder();
            for(char c : s.toCharArray()){
                sb.append(c).append('/');
            }
            Path ifile = Paths.get(dataPath.toString(), "index", sb.toString(), "idx.discordb");
            Files.deleteIfExists(ifile);
            Files.createDirectories(ifile.getParent());
            Files.createFile(ifile);
            DataOutputStream idx = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(ifile), bufferSize));
            List<Integer> entries = index.get(dict.get(s));
            for(int e : entries){
                idx.writeInt(e);
            }
            idx.flush();
            idx.close();
        }
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

    private void close(DataOutputStream dos) throws IOException {
        dos.flush();
        dos.close();
    }

    private void createMapping(){
        tempMap = new int[dict.size()];
        map = new String[dict.size()];
        index = new ArrayList<List<Integer>>();
        int newVal = 0;
        for(String s : dict.keySet()){
            map[newVal] = s;
            index.add(newVal, new ArrayList<Integer>());
            int currentVal = dict.get(s);
            dict.put(s, newVal);
            tempMap[currentVal] = newVal++;
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
        close(tempData);
    }

    private void parseTempData() throws IOException {
        nextDataFile();
        Files.walkFileTree(dataPath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                DataInputStream tmp = new DataInputStream(new BufferedInputStream(Files.newInputStream(file), bufferSize));
                int pos = 0;
                while(tmp.available() > 0){
                    int i = tmp.readInt();
                    data.writeInt(tempMap[i]);
                    List<Integer> indexEntry = index.get(tempMap[i]);
                    indexEntry.add(pos);
                    pos++;
                }
                tmp.close();
                return FileVisitResult.CONTINUE;
            }
        });
        close(data);
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

    private DataInputStream getIndexInputStream(String s) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(char c : s.toCharArray()){
            sb.append(c).append('/');
        }
        Path ifile = Paths.get(dataPath.toString(), "index", sb.toString(), "idx.discordb");
        return new DataInputStream(Files.newInputStream(ifile));
    }

    public void lookup(String s) throws IOException {
        long start = System.currentTimeMillis();
        int val = dict.get(s);
        DataInputStream idx = getIndexInputStream(s);
        DataInputStream dat = new DataInputStream(Files.newInputStream(dataFile));
        int datPos = 0;
        List<List<String>> lines = new ArrayList<List<String>>();
        for(int i = 0; i < 10; i++){
            int toSkip = (idx.readInt() * 4) - datPos;
            datPos += toSkip;
            dat.skipBytes(toSkip);
            List<String> cl = new ArrayList<String>();
            for(int j = 0; j <5; j++){
                int wordVal = dat.readInt();
                cl.add(map[wordVal]);
                datPos += 4;
            }
            lines.add(cl);
        }
        idx.close();
        dat.close();
        long end = System.currentTimeMillis();
        System.out.println(s + " found in " + (end - start) + "ms:");
        for(List<String> l : lines){
            System.out.println(l);
        }
    }

        /*
    public void lookupMem(String s) throws IOException {
        long start = System.currentTimeMillis();
        int val = dict.get(s);
        List<Integer> indexEntry = index.get(val);
        DataInputStream dat = new DataInputStream(Files.newInputStream(dataFile));
        int datPos = 0;
        List<List<String>> lines = new ArrayList<List<String>>();
        for(int i = 0; i < 10; i++){
            int toSkip = (indexEntry.get(i) * 4) - datPos;
            datPos += toSkip;
            dat.skipBytes(toSkip);
            List<String> cl = new ArrayList<String>();
            for(int j = 0; j <5; j++){
                int wordVal = dat.readInt();
                cl.add(map[wordVal]);
                datPos += 4;
            }
            lines.add(cl);
        }
        long end = System.currentTimeMillis();
        System.out.println(indexEntry.size() + " occurances of " + s + " found in " + (end - start) + "ms:");
        for(List<String> l : lines){
            System.out.println(l);
        }
    }*/
}
