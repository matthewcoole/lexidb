package uk.ac.lancs.ucrel.conv;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class HansardConv {

    private static final Logger LOG = LogManager.getLogger(HansardConv.class);

    public static Path OUT = Paths.get("/home/mpc/Desktop/lob");
    public static int outFileCount =0;
    public static int wordCount = 0;
    public static List<String> allLines;
    public static DecimalFormat headF, tailF;
    public static int LIMIT = 1000000;
    public static long last;

    public static void main(String[] args) throws IOException {
        Path in = Paths.get("/home/mpc/Desktop/lob_");
        Files.createDirectories(OUT);
        headF = new DecimalFormat("00");
        tailF = new DecimalFormat("0000");
        allLines = new ArrayList<String>();
        last = System.currentTimeMillis();
        Files.walkFileTree(in,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                List<String> input = Files.readAllLines(file, StandardCharsets.UTF_8);
                List<String> conv = new ArrayList<String>(input.size());
                for(String s : input){
                    String newS = StringEscapeUtils.unescapeHtml(s);
                    newS = newS.replaceAll("[^A-Za-z ]", "");
                    newS = newS.trim();
                    newS = newS.toLowerCase();
                    StringTokenizer st = new StringTokenizer(newS);
                    wordCount += st.countTokens();
                    conv.add(newS);
                }
                allLines.addAll(conv);
                if(wordCount >= LIMIT){
                    writeOutput();
                    allLines = new ArrayList<String>();
                    wordCount = 0;
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void writeOutput() throws IOException {
        String h = headF.format(outFileCount%4);
        String t = tailF.format(outFileCount/4);
        String filename = h + "_" + t + ".txt";
        Path of = Paths.get(OUT.toString(), filename);
        Files.deleteIfExists(of);
        Files.createFile(of);
        Files.write(of, allLines, StandardCharsets.UTF_8);
        long now = System.currentTimeMillis();
        LOG.info(filename + " (" + outFileCount + ") written to disk in " + (now - last) + "ms");
        last = now;
        outFileCount++;
    }
}
