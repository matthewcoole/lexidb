package storage;

import me.lemire.integercompression.differential.IntegratedIntCompressor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Pair;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Index {
    private static Logger LOG = LoggerFactory.getLogger(Index.class);
    public boolean enabled = true;
    Map<Integer, List<Integer>> entries = new LinkedHashMap<>();
    private Path path;

    public void setPath(Path p) {
        this.path = p;
    }

    public void add(int numericValue, int pos) {
        if (!entries.containsKey(numericValue))
            entries.put(numericValue, new ArrayList<>());
        entries.get(numericValue).add(pos);
    }

    public int[] lookup(List<Integer> numericValues) throws IOException {
        Collections.sort(numericValues);

        long start = System.currentTimeMillis();
        DataInputStream dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(path.toString() + ".ilu"))));
        List<Pair> lookup = new ArrayList<>();
        int inputPos = 0;
        int lastVal = 0;
        for (int value : numericValues) {
            if (!(value < 0)) {
                try {
                    int bytesToSkip = (value * 4) - inputPos;
                    int numericValue;
                    if (bytesToSkip == -4) {
                        numericValue = lastVal;
                    } else {
                        dis.skipBytes(bytesToSkip);
                        inputPos += bytesToSkip;
                        numericValue = dis.readInt();
                        inputPos += 4;
                    }
                    int next = dis.readInt();
                    inputPos += 4;
                    lookup.add(new Pair(numericValue, next));
                    lastVal = next;
                } catch (EOFException e) {
                }
            }
        }
        dis.close();
        long end = System.currentTimeMillis();
        long iluTime = end - start;

        IntegratedIntCompressor iic = new IntegratedIntCompressor();
        inputPos = 0;
        List<int[]> blocks = new ArrayList<>();

        start = System.currentTimeMillis();
        dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(path.toString() + ".idx"))));
        for (Pair p : lookup) {
            if (!((Integer) p.getKey() < 0)) {
                try {
                    int bytesToSkip = ((Integer) p.getKey() * 4) - inputPos;
                    dis.skipBytes(bytesToSkip);

                    int intsToRead = (Integer) p.getValue() - (Integer) p.getKey();
                    int[] compressedData = new int[intsToRead];

                    for (int i = 0; i < intsToRead; i++) {
                        compressedData[i] = dis.readInt();
                    }
                    blocks.add(compressedData);

                    inputPos = inputPos + bytesToSkip + (intsToRead * 4);
                } catch (EOFException e) {
                }
            }
        }
        dis.close();
        end = System.currentTimeMillis();
        long idxTime = end - start;

        start = System.currentTimeMillis();
        int capacity = 0;
        for (int i = 0; i < blocks.size(); i++) {
            int[] data = iic.uncompress(blocks.get(i));
            blocks.set(i, data);
            capacity += data.length;
        }
        end = System.currentTimeMillis();
        long decompressTime = end - start;

        start = System.currentTimeMillis();
        int[] allPositions = new int[capacity];
        int count = 0;
        for (int[] d : blocks) {
            for (int i : d) {
                allPositions[count] = i;
                count++;
            }
        }
        Arrays.parallelSort(allPositions);
        end = System.currentTimeMillis();
        long groupingTime = end - start;

        LOG.debug(String.format("Time reading from ilu: %dms\n", iluTime));
        LOG.debug(String.format("Time reading from idx: %dms\n", idxTime));
        LOG.debug(String.format("Time decompressing: %dms\n", decompressTime));
        LOG.debug(String.format("Time grouping data: %dms\n", groupingTime));

        return allPositions;
    }

    public void save() throws IOException {
        DataOutputStream allEntries = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(path.toString() + ".idx"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
        DataOutputStream lookup = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(path.toString() + ".ilu"), StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
        IntegratedIntCompressor iic = new IntegratedIntCompressor();
        int length = 0;
        for (int i = 0; i < entries.size(); i++) {
            lookup.writeInt(length);
            List<Integer> entry = entries.get(i);
            int[] data = new int[entry.size()];
            for (int j = 0; j < data.length; j++) {
                data[j] = entry.get(j);
            }
            int[] compressedData = iic.compress(data);
            for (int j : compressedData) {
                allEntries.writeInt(j);
                length++;
            }
        }
        lookup.writeInt(length);
        allEntries.close();
        lookup.close();
    }
}
