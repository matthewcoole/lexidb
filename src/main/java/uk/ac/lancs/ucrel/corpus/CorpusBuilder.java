package uk.ac.lancs.ucrel.corpus;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.region.RegionBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CorpusBuilder {

    private static final Logger LOG = LogManager.getLogger(CorpusBuilder.class);

    private Path corpusPath;
    private DecimalFormat regionNameFormatter;
    private int regionCount;

    public CorpusBuilder(Path corpusPath) throws IOException {
        this.corpusPath = corpusPath;
        regionNameFormatter = new DecimalFormat("0000");
        delete();
        Files.createDirectories(corpusPath);
    }

    public void addRegion(List<String> words) throws IOException, InterruptedException {
        RegionBuilder rb = new RegionBuilder(Paths.get(corpusPath.toString(), regionNameFormatter.format(regionCount++)));
        rb.add(words);
        rb.build();
        rb.save();
    }

    private void delete() throws IOException {
        LOG.info(corpusPath);
        Files.walkFileTree(corpusPath, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                LOG.info(file.toString());
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException
            {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
