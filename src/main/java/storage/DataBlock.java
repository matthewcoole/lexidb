package storage;

import lombok.extern.slf4j.Slf4j;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class DataBlock {

    public String corpus;
    public String name;
    public List<ColumnSet> sets = new ArrayList<>();
    private Path path;
    private transient int currentSize;

    public int size() {
        return currentSize;
    }

    public Path retrievePath() {
        return path;
    }

    public void setPath(Path p) {
        this.path = p;
        for (ColumnSet cs : sets) {
            cs.setPath(Paths.get(p.toString()));
        }
    }

    public String memberOfSet(String column) {
        for (ColumnSet cs : sets) {
            if (cs.containsAny(column))
                return cs.name;
        }
        return null;
    }

    public void addFile(File file, String dir) throws IOException {
        Iterable<CSVRecord> records;
        if (file.getName().endsWith("tsv")) {
            records = CSVFormat.TDF.withFirstRecordAsHeader().parse(Files.newBufferedReader(file.toPath()));
        } else
            records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(Files.newBufferedReader(file.toPath()));
        String relativePath = file.getAbsolutePath().replace(dir, "");
        add(records, relativePath);
    }

    public void add(Iterable<CSVRecord> records, String file) throws IOException {
        Map<String, String> xml = new HashMap<>();
        try {
            for (CSVRecord r : records) {
                extractXML(xml, r);
                Map<String, String> map = r.toMap();
                addXml(map, xml, sets);
                map.put("$file", file);
                for (ColumnSet cs : sets) {
                    cs.add(map);
                }
                currentSize++;
            }
        } catch (Exception e) {
            System.err.println("File: " + file + " caused a problem!");
            e.printStackTrace();

        }
    }

    private void addXml(Map<String, String> map, Map<String, String> xml, List<ColumnSet> sets) {
        for (ColumnSet cs : sets) {
            for (Column c : cs.columns) {
                if (c.xml != null && !c.xml.isEmpty()) {
                    map.put(c.name, xml.get(c.xml));
                }
            }
        }
    }

    public String[] listAllColumns() {
        List<String> columns = new ArrayList<>();
        for (ColumnSet cs : sets) {
            columns.addAll(cs.listColumns());
        }
        return columns.toArray(new String[0]);
    }

    private void extractXML(Map<String, String> xml, CSVRecord r) {
        String token = r.get(0).trim();
        try {
            if (token.matches("<\\?(\\S+)[^\\?]*\\?>")) {
                String pi = token.replace("<?", "").replace("?>", "");
                String[] bits = pi.split(" ");
                xml.put("?" + bits[0], bits[1]);
            }
            if (token.matches("<(\\S+?)[^>]*>")) {
                token = token.replace("<", "");
                token = token.replace(">", " ");

                String element = token.substring(0, token.indexOf(' '));

                token = token.substring(token.indexOf(' ')).trim();

                Map<String, String> attr = new HashMap<>();

                while (!token.isEmpty() && token.indexOf("=\"") != -1) {
                    String aName = token.substring(0, token.indexOf("=\""));
                    token = token.substring(token.indexOf("=\"") + 2).trim();
                    String aVal = token.substring(0, token.indexOf('"'));
                    token = token.substring(token.indexOf('"') + 1).trim();
                    attr.put(aName, aVal);
                }

                for (String key : attr.keySet()) {
                    xml.put(element + "/@" + key, attr.get(key));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() throws IOException, ClassNotFoundException {
        for (ColumnSet cs : sets) {
            cs.load();
        }
    }

    public void complete() throws IOException {
        for (ColumnSet cs : ProgressBar.wrap(sets, "Completing  ")) {
            cs.complete();
        }
    }

    public void saveConf() throws IOException {
        JSONUtil.savetoJSON(Paths.get(path.toString(), "conf.json"), this);
    }

    public void save() throws IOException {
        for (ColumnSet cs : ProgressBar.wrap(sets, "Saving      ")) {
            cs.save();
        }
        JSONUtil.savetoJSON(Paths.get(path.toString(), "conf.json"), this);
    }

    public List<List<Map<String, String>>> getList(List<List<Integer>> rowIndices, String... columns) throws IOException {
        List<List<Map<String, String>>> rows = new ArrayList<>();
        TreeSet<Integer> allIndices = new TreeSet<>();
        for (List<Integer> li : rowIndices) {
            allIndices.addAll(li);
        }
        Map<Integer, Map<String, String>> valueMap = get(allIndices, columns);

        for (List<Integer> li : rowIndices) {
            List<Map<String, String>> kwic = new ArrayList<>();
            for (int i : li) {
                kwic.add(valueMap.get(i));
            }
            rows.add(kwic);
        }
        return rows;
    }

    public Map<Integer, Map<String, String>> get(TreeSet<Integer> indices, String... columns) throws IOException {
        Map<Integer, Map<String, String>> vals = new HashMap<>();
        for (ColumnSet cs : sets) {
            if (cs.containsAny(columns)) {
                Map<Integer, Map<String, String>> colVals = cs.get(indices);
                for (Integer i : colVals.keySet()) {
                    if (vals.containsKey(i)) {
                        vals.get(i).putAll(colVals.get(i));
                    } else {
                        vals.put(i, colVals.get(i));
                    }
                }

            }
        }
        return vals;
    }

    public List<Map<String, String>> get(List<Integer> rowIndices) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 0; i < rowIndices.size(); i++) {
            rows.add(new LinkedHashMap<>());
        }
        for (ColumnSet cs : sets) {
            List<Map<String, String>> setVals = cs.get(rowIndices);
            for (int i = 0; i < rows.size(); i++) {
                rows.get(i).putAll(setVals.get(i));
            }
        }
        return rows;
    }

    public int finalCount(Map<String, String> vals) {
        for (ColumnSet cs : sets) {
            if (cs.containsAny(vals.keySet().toArray(new String[0]))) {
                return cs.finalCount(vals);
            }
        }
        return 0;
    }

    public int finalCount(String key, String val) {
        Map<String, String> kvp = new HashMap<>();
        kvp.put(key, val);
        return finalCount(kvp);
    }

    public int finalCount() {
        return sets.get(0).finalCount();
    }

    public Map<String, Object> corpusSize() {
        Map<String, Object> result = new HashMap<>();
        int records = sets.get(0).getDictionary().getRecordCount();
        result.put("count", records);
        Map<String, Integer> types = new HashMap<>();
        for (ColumnSet cs : sets) {
            int typeCount = cs.getDictionary().getTypeCount();
            types.put(cs.name, typeCount);
        }
        result.put("types", types);
        return result;
    }

    public Map<String, List<Integer>> lookupNumericValues(Map<String, String> values) {
        Map<String, List<Integer>> results = new HashMap<>();
        for (ColumnSet cs : sets) {
            List<Integer> numericValues = cs.lookup(values);
            if (numericValues != null && numericValues.size() > 0)
                results.put(cs.name, numericValues);
        }
        return results;
    }

    public String[] getValues(List<Integer> numericValues, String set) {
        String[] result = new String[numericValues.size()];
        for (ColumnSet cs : sets) {
            if (cs.name.equals(set)) {
                for (int i = 0; i < numericValues.size(); i++) {
                    result[i] = cs.getDictionary().getEntry(numericValues.get(i)).values.values().iterator().next();
                }
            }
        }
        return result;
    }

    public int[] getFrequencyList(List<Integer> numericValues, String set) {
        int[] result = new int[numericValues.size()];
        for (ColumnSet cs : sets) {
            if (cs.name.equals(set)) {
                for (int i = 0; i < numericValues.size(); i++) {
                    result[i] = cs.finalCount(numericValues.get(i));
                }
            }
        }
        return result;
    }

    public int[] lookupPositions(List<Integer> numericValues, String setName) throws IOException {
        int[] positions = {};
        for (ColumnSet cs : sets) {
            if (cs.name.equals(setName)) {
                return cs.lookup(numericValues);
            }
        }
        return positions;
    }
}
