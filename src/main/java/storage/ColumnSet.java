package storage;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class ColumnSet {

    private static Logger LOG = LoggerFactory.getLogger(ColumnSet.class);

    public String name;
    public boolean rle;
    public ArrayList<Column> columns = new ArrayList<>();
    public int valueLength = 4;
    private Path path;
    private Index index = new Index();
    private Dictionary dict = new Dictionary();
    private DataOutputStream dos;
    private int last;
    private int length;
    private int[] rleInMem;

    public void add(Map<String, String> record) throws IOException {
        if (dos == null) {
            Path p = Paths.get(path.toString(), name + ".tmp");
            p.toFile().getParentFile().mkdirs();
            dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(p, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
        }
        Map<String, String> vals = new LinkedHashMap<>();
        for (Column c : columns) {
            String val = "";
            if (record.containsKey(c.name)) {
                val = record.get(c.name);
            }
            if (c.clean != null) {
                val = val.replaceAll(c.clean, "");
            }
            if (val == null || val.length() == 0)
                val = "$NULL";
            if (val.charAt(0) == '<' && val.endsWith(">")) {
                StringBuilder sb = new StringBuilder();
                String[] bits = val.split(" ");
                sb.append(bits[0]);
                if (bits.length > 1) {
                    if (val.charAt(val.length() - 2) == '/')
                        sb.append('/');
                    sb.append('>');
                    val = sb.toString();
                }
            }
            vals.put(c.name, val);
        }
        int i = dict.add(vals);
        if (!rle)
            dos.writeInt(i);
        else {
            if (i != last) {
                dos.writeInt(last);
                dos.writeInt(length);
                last = i;
                length = 1;
            } else
                length++;
        }
    }

    public boolean hasColumn(String columnName) {
        for (Column c : columns) {
            if (columnName.equals(c.name))
                return true;
        }
        return false;
    }

    public void load() throws IOException, ClassNotFoundException {
        dict.load();
    }

    public void complete() throws IOException {
        if (rle) {
            completeRle();
        } else
            completeNonRle();
    }

    private void completeNonRle() throws IOException {
        dos.flush();
        dos.close();
        dict.generateFinalVals();
        valueLength = calculateMinimumValueLength();
        Path tmp = Paths.get(path.toString(), name + ".tmp");
        Path out = Paths.get(path.toString(), name + ".dat");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(tmp)));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));

        int pos = 0;
        while (dis.available() > 0) {
            int i = dis.readInt();
            int finalVal = dict.getFinalValue(i);
            dict.incrementCount(finalVal);
            //dos.writeInt(finalVal);
            writeValue(finalVal, dos);
            index.add(finalVal, pos++);
        }
        dis.close();
        dos.close();
        tmp.toFile().delete();
    }

    private void completeRle() throws IOException {
        dos.writeInt(last);
        dos.writeInt(length);
        dos.flush();
        dos.close();
        dict.generateFinalVals();
        Path tmp = Paths.get(path.toString(), name + ".tmp");
        Path out = Paths.get(path.toString(), name + ".dat");
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(tmp)));
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
        int pos = 0;
        while (dis.available() > 0) {
            int i = dis.readInt();
            int length = dis.readInt();
            int finalVal = dict.getFinalValue(i);
            dict.incrementCount(finalVal);
            dos.writeInt(finalVal);
            dos.writeInt(length);
            int endPos = pos + length;
            while (pos < endPos) {
                index.add(finalVal, pos++);
            }
        }
        dis.close();
        dos.close();
        tmp.toFile().delete();
    }

    private void writeValue(int val, DataOutputStream dos) throws IOException {
        if (valueLength == 1) {
            dos.writeByte(val + Byte.MIN_VALUE);
        }
        if (valueLength == 2) {
            dos.writeShort(val + Short.MIN_VALUE);
        }
        if (valueLength == 4) {
            dos.writeInt(val + Integer.MIN_VALUE);
        }
    }

    private int calculateMinimumValueLength() {
        if (dict.size() + Byte.MIN_VALUE < Byte.MAX_VALUE)
            return 1;
        if (dict.size() + Short.MIN_VALUE < Short.MAX_VALUE)
            return 2;
        if (dict.size() + Integer.MIN_VALUE < Integer.MAX_VALUE)
            return 4;
        return 8;
    }

    public void save() throws IOException {
        dict.save();
        index.save();
    }

    @NotNull
    private Map<String, String> numericValueToRowMap(int numericValue) {
        try {
            DictionaryEntry de = dict.getEntry(numericValue);
            Map<String, String> row = new LinkedHashMap<>();
            for (String key : de.values.keySet()) {
                if (!de.values.get(key).equals("$NULL"))
                    row.put(key, de.values.get(key));
            }
            return row;
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    public List<Map<String, String>> get(List<Integer> rowIndices) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        Map<Integer, Map<String, String>> rowMap = get((Collection) rowIndices);
        for (int i : rowIndices) {
            rows.add(rowMap.get(i));
        }
        return rows;
    }

    public int finalCount(Map<String, String> vals) {
        return dict.getFinalCount(vals);
    }

    public int finalCount(int numericValue) {
        return dict.getFinalCount(numericValue);
    }

    public int finalCount() {
        return dict.getFinalCount();
    }

    public Map<Integer, Map<String, String>> get(Collection<Integer> rowIndices) throws IOException {
        if (rle)
            return getRle(rowIndices);
        else
            return getNonRle(rowIndices);
    }

    private Map<Integer, Map<String, String>> getRle(Collection<Integer> rowIndices) throws IOException {
        Map<Integer, Map<String, String>> rows = new HashMap<>();
        Path dat = Paths.get(path.toString(), name + ".dat");

        if(rleInMem == null){
            FileChannel fc = FileChannel.open(dat);
            MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            rleInMem = new int[buf.capacity()/4];
            int i = 0;
            while(buf.hasRemaining()){
                rleInMem[i] = buf.getInt();
                i++;
            }
            fc.close();
        }

        int lastVal = 0;
        int currentIndex = 0;
        int posInRleInMem = 0;
        for (int index : rowIndices) {
            try {
                Map<String, String> row = new LinkedHashMap<>();
                while (currentIndex <= index) {
                    lastVal = rleInMem[posInRleInMem++];
                    currentIndex += rleInMem[posInRleInMem++];
                }
                row = numericValueToRowMap(lastVal);
                //row.put("i", Integer.toString(index));
                rows.put(index, row);
            } catch (Exception e){
                LOG.debug("Spooled off end of corpus");
            }
        }

        return rows;
    }

    private Map<Integer, Map<String, String>> getNonRle(Collection<Integer> rowIndices) throws IOException {
        Map<Integer, Map<String, String>> rows = new HashMap<>();
        Path dat = Paths.get(path.toString(), name + ".dat");

        FileChannel fc = FileChannel.open(dat);
        MappedByteBuffer buf = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

        for (int index : rowIndices) {
            Map<String, String> row = new LinkedHashMap<>();
            try {
                int numericValue = getNumericValue(buf, index);
                //int numericValue = buf.getInt(index*4);
                row = numericValueToRowMap(numericValue);
                //row.put("i", Integer.toString(index));
                rows.put(index, row);
            } catch (Exception e) {
                LOG.debug("Spooled off end of corpus");
            }
        }

        fc.close();
        return rows;
    }

    private int getNumericValue(MappedByteBuffer b, int index) {
        int val = 0;
        if (valueLength == 1)
            val = b.get(index) - Byte.MIN_VALUE;
        if (valueLength == 2)
            val = b.getShort(index * 2) - Short.MIN_VALUE;
        if (valueLength == 4)
            val = b.getInt(index * 4) - Integer.MIN_VALUE;
        return val;
    }

    public int[] lookup(List<Integer> numericValues) throws IOException {
        return index.lookup(numericValues);
    }

    public List<Integer> lookup(Map<String, String> values) {
        Map<String, String> cleanMap = new LinkedHashMap<>();
        for (String key : values.keySet()) {
            for (Column c : columns) {
                if (c.name.equals(key))
                    cleanMap.put(key, values.get(key));
            }
        }
        return dict.getNumericValues(cleanMap);
    }

    public boolean containsAny(String... columns) {
        for (Column c : this.columns) {
            for (String column : columns) {
                if (c.name.equals(column))
                    return true;
            }
        }
        return false;
    }

    protected Dictionary getDictionary() {
        return dict;
    }

    public void setPath(Path p) {
        this.path = p;
        index.setPath(Paths.get(p.toString(), name));
        dict.setPath(Paths.get(p.toString(), name));
        dict.setName(this.name);
    }

    public List<String> listColumns() {
        List<String> columns = new ArrayList<>();
        for (Column c : this.columns) {
            columns.add(c.name);
        }
        return columns;
    }
}
