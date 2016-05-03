package uk.ac.lancs.ucrel;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.parser.TextParser;
import uk.ac.lancs.ucrel.sort.ConcLineComparator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger.getLogger(Main.class).info("Check logger");
        System.out.println("This will be the main class");

        //parse();

        search("the");
    }

    private static void parse() throws IOException {
        TextParser tp = new TextParser(Paths.get("/home/mpc/Desktop/data"));
        tp.parse(Paths.get("/home/mpc/Desktop/bnc"));
    }

    private static void search(String keyword) throws IOException {
        CorpusAccessor ca = new CorpusAccessor(Paths.get("/home/mpc/Desktop/data"));
        long start = System.currentTimeMillis();
        List<int[]> results = ca.search(keyword, 0);
        //Collections.sort(results, new ConcLineComparator(1));
        long end = System.currentTimeMillis();

        System.out.println("Search for \"" + keyword + "\" took " + (end - start) + "ms, count=" + results.size());

        for(int i = 0;i < results.size() && i < 20; i++){
            System.out.println(ca.getLineAsString(results.get(i)));
        }
    }
}
