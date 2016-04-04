package uk.ac.lancs.ucrel.corpus;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CorpusAccessorTest {

    private String[] words = {"aaan", "gfck", "ozmp", "xciu"};
    private Map<String, Long> avgTimes = new TreeMap<String, Long>();
    private int limit = 1, runs = 1;

    @Test
    public void testSearch() throws IOException {
        CorpusAccessor ca = new CorpusAccessor(Paths.get("/home/mpc/data"));
        for(String s : words){
            avgTimes.put(s, 0L);
            for(int i = 0; i < runs; i++){
                long start = System.currentTimeMillis();
                ca.search(s, limit, false);
                long end = System.currentTimeMillis();
                long avg = avgTimes.get(s);
                avg = ((avg * i) + (end - start)) / (i + 1);
                avgTimes.put(s, avg);
            }
        }
        System.out.println(avgTimes);
    }
}
