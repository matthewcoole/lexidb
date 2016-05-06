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
    }
}
