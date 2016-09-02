package uk.ac.lancs.ucrel.conv;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by mpc on 02/09/16.
 */
public class Results {

    private static Map<String, String[]> results = new HashMap<String, String[]>();

    public static void main(String[] args) throws Exception{
        Path dir = Paths.get("/home/mpc/Desktop/results");
        Files.walkFileTree(dir ,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    List<String> lines = Files.readAllLines(file);
                    String filename = file.getFileName().toString();
                    filename = filename.substring(filename.length() -1, filename.length());
                    System.out.println(filename);
                    for(String line:lines){
                        String[] bits = line.split("\t");
                        if(!results.containsKey(bits[0]))
                            results.put(bits[0], new String[5]);
                        results.get(bits[0])[Integer.parseInt(filename) - 1] = bits[1];
                    }

                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        List<String> sortedKeys = new ArrayList<String>();
        sortedKeys.addAll(results.keySet());
        Collections.sort(sortedKeys);
        List<String> lines = new ArrayList<String>();
        lines.add("word,1,2,3,4,5");
        for(String key : sortedKeys){
            StringBuilder sb = new StringBuilder(key);
            for(String s : results.get(key)){
                sb.append(",").append(s);
            }
            lines.add(sb.toString());
        }
        Files.write(Paths.get("/home/mpc/Desktop/results/all_conc_results.csv"), lines);
        System.out.println(results);
    }
}
