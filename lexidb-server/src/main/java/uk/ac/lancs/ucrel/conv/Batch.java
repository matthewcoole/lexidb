package uk.ac.lancs.ucrel.conv;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Batch {

    public static final Path OUT = Paths.get("/home/mpc/Desktop/bnc_tag");
    public static int LIMIT = 1000000;
    public static DecimalFormat df;
    public static List<String> lines = new ArrayList<String>();
    public static int count = 0;

    public static void main(String[] args) throws IOException {
        Files.createDirectories(OUT);
        Path in = Paths.get("/home/mpc/Desktop/bnc_tagged");
        df = new DecimalFormat("0000");
        Files.walkFileTree(in,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    lines.addAll(Files.readAllLines(file));
                    if(lines.size() > LIMIT){
                        Path of = Paths.get(OUT.toString(), df.format(count) + ".tsv");
                        Files.write(of, lines);
                        System.out.println(of.toString());
                        lines = new ArrayList<String>();
                        count++;
                    }
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        if(lines.size() > 0){
            Path of = Paths.get(OUT.toString(), df.format(count) + ".tsv");
            Files.write(of, lines);
        }
    }

}
