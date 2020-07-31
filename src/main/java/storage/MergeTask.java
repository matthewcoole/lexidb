package storage;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import util.ZipfianComparator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MergeTask {
    private DataBlock a, b, c;
    private int newDictionarySize;
    private Map<String, int[]> mappingsFromA, mappingsFromB;
    private boolean complete;

    public MergeTask(DataBlock a, DataBlock b, DataBlock c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public void run() {
        try {
            mergeDictionaries();
            mapData();
            complete = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mapData() throws IOException {
        for (ColumnSet cs : c.sets) {
            Path outputPath = Paths.get(c.retrievePath().toString(), cs.name + ".dat");
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(outputPath)));
            Index idx = new Index();
            idx.setPath(Paths.get(c.retrievePath().toString(), cs.name));
            int currentPos = 0;
            currentPos = map(dos, cs.rle, cs.name, a, mappingsFromA.get(cs.name), cs.valueLength, idx, currentPos);
            map(dos, cs.rle, cs.name, b, mappingsFromB.get(cs.name), cs.valueLength, idx, currentPos);
            dos.close();
            idx.save();
        }
        c.saveConf();
    }

    private int map(DataOutputStream dos, boolean rle, String name, DataBlock css, int[] mapping, int recordLength, Index idx, int pos) throws IOException {
        Path dataPath = Paths.get(css.retrievePath().toString(), name + ".dat");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(dataPath)));
        if (!rle)
            return map(dis, mapping, recordLength(mapping.length), recordLength, dos, idx, pos);
        else
            return mapRle(dis, mapping, dos, idx, pos);
    }

    private void writeValue(int val, DataOutputStream dos, int recordLength) throws IOException {
        if (recordLength == 1) {
            dos.writeByte(val + Byte.MIN_VALUE);
        }
        if (recordLength == 2) {
            dos.writeShort(val + Short.MIN_VALUE);
        }
        if (recordLength == 4) {
            dos.writeInt(val + Integer.MIN_VALUE);
        }
    }

    private int map(DataInputStream dis, int[] mapping, int initialRecordLength, int newRecordLength, DataOutputStream dos, Index idx, int pos) throws IOException {
        //List<Integer> data = new ArrayList<>();
        while (dis.available() > 0) {
            int i = mapping[readRecord(dis, initialRecordLength)];
            writeValue(i, dos, newRecordLength);
            //data.add(i);
            idx.add(i, pos++);
        }
        return pos;
    }

    private int mapRle(DataInputStream dis, int[] mapping, DataOutputStream dos, Index idx, int pos) throws IOException {
        //List<Integer> data = new ArrayList<>();
        //List<Integer> lengths = new ArrayList<>();
        while (dis.available() > 0) {
            int val = mapping[dis.readInt()];
            int length = dis.readInt();
            dos.writeInt(val);
            dos.writeInt(length);
            //data.add(val);
            //lengths.add(length);
            idx.add(val, pos++);
        }
        return pos;
    }

    private int readRecord(DataInputStream dis, int recordLength) throws IOException {
        int val = 0;
        if (recordLength == 1)
            val = dis.readByte() - Byte.MIN_VALUE;
        if (recordLength == 2)
            val = dis.readShort() - Short.MIN_VALUE;
        if (recordLength == 4)
            val = dis.readInt() - Integer.MIN_VALUE;
        return val;
    }

    private int recordLength(int dictSize) {
        if (dictSize + Byte.MIN_VALUE < Byte.MAX_VALUE)
            return 1;
        if (dictSize + Short.MIN_VALUE < Short.MAX_VALUE)
            return 2;
        if (dictSize + Integer.MIN_VALUE < Integer.MAX_VALUE)
            return 4;
        return 8;
    }

    private void mergeDictionaries() throws IOException {
        mappingsFromA = new HashMap<>();
        mappingsFromB = new HashMap<>();
        for (ColumnSet cs : c.sets) {
            Path dictA = Paths.get(a.retrievePath().toString(), cs.name + ".dict");
            Path dictB = Paths.get(b.retrievePath().toString(), cs.name + ".dict");
            Iterable<CSVRecord> entriesA = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(Files.newBufferedReader(dictA));
            Iterable<CSVRecord> entriesB = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(Files.newBufferedReader(dictB));

            Map<DictionaryEntry, Integer> numericValuesA = new TreeMap<>(new ZipfianComparator());
            Map<DictionaryEntry, Integer> numericValuesB = new TreeMap<>(new ZipfianComparator());
            Map<DictionaryEntry, Integer> numericValuesC = new TreeMap<>(new ZipfianComparator());
            int numericValA = 0, numericValB = 0, numericValC = 0;

            Map<DictionaryEntry, Integer> counts = new TreeMap<>(new ZipfianComparator());
            for (CSVRecord r : entriesA) {
                Map<String, String> recordMap = r.toMap();
                int count = Integer.parseInt(recordMap.remove("#"));
                DictionaryEntry de = new DictionaryEntry(recordMap);
                counts.put(de, count);
                numericValuesA.put(de, numericValA++);
            }
            for (CSVRecord r : entriesB) {
                Map<String, String> recordMap = r.toMap();
                int count = Integer.parseInt(recordMap.remove("#"));
                DictionaryEntry de = new DictionaryEntry(recordMap);
                numericValuesB.put(de, numericValB++);
                if (counts.containsKey(de)) {
                    count += counts.get(de);
                    counts.replace(de, count);
                } else {
                    counts.put(de, count);
                }
            }

            for (DictionaryEntry de : counts.keySet()) {
                numericValuesC.put(de, numericValC++);
            }
            cs.valueLength = recordLength(numericValC);

            mappingsFromA.put(cs.name, generateMapping(numericValuesA, numericValuesC));
            mappingsFromB.put(cs.name, generateMapping(numericValuesB, numericValuesC));

            c.retrievePath().toFile().mkdirs();
            c.retrievePath().toFile().mkdir();
            BufferedWriter bw = Files.newBufferedWriter(Paths.get(c.retrievePath().toString(), cs.name + ".dict"));
            List<String> headers = new ArrayList<>(counts.keySet().iterator().next().values.keySet());
            headers.add("#");
            CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
            for (DictionaryEntry de : counts.keySet()) {
                List<String> vals = new ArrayList<>(de.values.values());
                vals.add(String.valueOf(counts.get(de)));
                printer.printRecord(vals);
            }
            printer.close();
        }
    }

    private int[] generateMapping(Map<DictionaryEntry, Integer> a, Map<DictionaryEntry, Integer> b) {
        int[] aToB = new int[a.size()];
        int i = 0;
        for (DictionaryEntry de : a.keySet()) {
            aToB[i++] = b.get(de);
        }
        return aToB;
    }

    public boolean isComplete() {
        return complete;
    }
}
