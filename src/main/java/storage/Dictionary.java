package storage;

import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
import com.googlecode.concurrenttrees.radix.node.Node;
import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharSequenceNodeFactory;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import util.Pair;
import util.ZipfianComparator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Dictionary {

    private Map<DictionaryEntry, Integer> entries = new TreeMap<>(new ZipfianComparator());
    private String name;
    private Path path;
    private int[] initToFinalMap;
    private int[] finalCounts;
    private int finalCount;
    private DictionaryEntry[] lookup;
    private Map<String, ConcurrentRadixTree<Integer>> lookupTries = new HashMap<>();

    public void setName(String name) {
        this.name = name;
    }

    public int add(Map<String, String> vals) {
        DictionaryEntry de = new DictionaryEntry(vals);
        if (!entries.containsKey(de)) {
            entries.put(de, entries.size());
        }
        return entries.get(de);
    }

    public void setPath(Path p) {
        this.path = p;
    }

    public int getTypeCount() {
        return finalCounts.length;
    }

    public int getRecordCount() {
        return getFinalCount();
    }

    public void generateFinalVals() {
        initToFinalMap = new int[entries.size()];
        finalCounts = new int[entries.size()];
        lookup = new DictionaryEntry[entries.size()];
        int count = 0;
        for (DictionaryEntry de : entries.keySet()) {
            initToFinalMap[entries.get(de)] = count;
            entries.replace(de, count);
            lookup[count] = de;
            count++;
        }
    }

    public int getFinalValue(int initialValue) {
        return initToFinalMap[initialValue];
    }

    public void incrementCount(int finalValue) {
        finalCounts[finalValue]++;
    }

    public DictionaryEntry getEntry(int i) {
        return lookup[i];
    }

    public int getFinalCount(Map<String, String> vals) {
        DictionaryEntry de = new DictionaryEntry(vals);
        if(entries.containsKey(de)){
            int numericValue = entries.get(de);
            return finalCounts[numericValue];
        } else
            return 0;
    }

    public int getFinalCount(int numericValue) {
        return finalCounts[numericValue];
    }

    public int getFinalCount() {
        return finalCount;
    }

    public void load() throws IOException {
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(Files.newBufferedReader(Paths.get(path.toString() + ".dict")));
        int count = 0;
        List<Integer> finalCounts = new ArrayList<>();
        for (CSVRecord r : records) {
            Map<String, String> recordMap = r.toMap();
            int finalCount = Integer.parseInt(recordMap.remove("#"));
            finalCounts.add(finalCount);
            this.finalCount += finalCount;
            for (String key : recordMap.keySet()) {
                if (!lookupTries.containsKey(key))
                    lookupTries.put(key, new ConcurrentRadixTree(new DefaultCharSequenceNodeFactory()));
                lookupTries.get(key).put(recordMap.get(key), count);
            }
            entries.put(new DictionaryEntry(recordMap), count);
            count++;
        }
        lookup = new DictionaryEntry[entries.size()];
        for (DictionaryEntry de : entries.keySet()) {
            lookup[entries.get(de)] = de;
        }
        this.finalCounts = new int[finalCounts.size()];
        for (int i = 0; i < finalCounts.size(); i++) {
            this.finalCounts[i] = finalCounts.get(i);
        }
    }

    private Set<Integer> run(ConcurrentRadixTree t, RunAutomaton a) {
        Queue<Pair> k = new LinkedBlockingQueue<>();
        Set<Integer> h = new HashSet<>();
        k.add(new Pair(a.getInitialState(), t.getNode()));
        while (!k.isEmpty()) {
            Pair x = k.poll();
            int state = (int) x.getKey();
            for (Node n : ((Node) x.getValue()).getOutgoingEdges()) {
                int istate = state;
                for (char c : n.getIncomingEdge().toString().toCharArray()) {
                    istate = a.step(istate, c);
                    if (istate == -1)
                        break;
                }
                if (istate == -1)
                    continue;
                if (a.isAccept(istate) && n.getValue() != null)
                    h.add((Integer) n.getValue());
                if (!n.getOutgoingEdges().isEmpty())
                    k.add(new Pair(istate, n));
            }
        }
        return h;
    }

    public List<Integer> getNumericValues(Map<String, String> values) {
        Map<String, RunAutomaton> regexes = new HashMap<>();
        for (String key : values.keySet()) {
            RegExp re = new RegExp(values.get(key));
            RunAutomaton ra = new RunAutomaton(re.toAutomaton());
            regexes.put(key, ra);
        }
        Set<Integer> vals = null;
        for (String key : regexes.keySet()) {
            if (vals == null)
                vals = run(lookupTries.get(key), regexes.get(key));
            else
                vals.retainAll(run(lookupTries.get(key), regexes.get(key)));
        }
        List<Integer> list = new ArrayList<Integer>();
        if (vals != null)
            list.addAll(vals);
        return list;
    }

    public void save() throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(Paths.get(path.toString() + ".dict"));
        List<String> headers = new ArrayList<>(entries.keySet().iterator().next().values.keySet());
        headers.add("#");
        CSVPrinter printer = new CSVPrinter(bw, CSVFormat.DEFAULT.withHeader(headers.toArray(new String[0])));
        int numericVal = 0;
        for (DictionaryEntry de : entries.keySet()) {
            List<String> vals = new ArrayList<>(de.values.values());
            vals.add(String.valueOf(finalCounts[numericVal]));

            printer.printRecord((Object[]) vals.toArray(new String[0]));
            numericVal++;
        }
        printer.close();
    }

    public int size() {
        return entries.size();
    }

    public String toString() {
        return entries.toString();
    }
}