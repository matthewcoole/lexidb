package uk.ac.lancs.ucrel.parser;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class TextParser {

    private static final Logger LOG = LogManager.getLogger(TextParser.class);
    private static final int REGION_SIZE = 1000000;

    private Path dataPath;
    private List<String> currentWords;
    private CorpusBuilder cb;

    public TextParser(Path dataPath) {
        this.dataPath = dataPath;
    }

    public void parse(Path src) throws IOException {
        long start = System.currentTimeMillis();
        cb = new CorpusBuilder(dataPath);
        currentWords = new ArrayList<String>();
        Files.walkFileTree(src, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
                for (String s : lines) {
                    StringTokenizer st = new StringTokenizer(s);
                    while (st.hasMoreTokens()) {
                        currentWords.add(st.nextToken());
                    }
                }
                if (currentWords.size() >= REGION_SIZE) {
                    cb.addRegion(currentWords);
                    currentWords = new ArrayList<String>();
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if (currentWords.size() > 0) {
            cb.addRegion(currentWords);
        }
        cb.build();
        cb.save();
        long end = System.currentTimeMillis();
        LOG.info("Parse of " + src.toString() + " took " + (end - start) + "ms");
    }
}
