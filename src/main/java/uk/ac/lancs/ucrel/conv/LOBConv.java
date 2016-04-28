package uk.ac.lancs.ucrel.conv;

import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class LOBConv {

    public static Path OUT = Paths.get("C:\\Users\\Matt\\Desktop\\lob_text");

    public static void main(String[] args) throws IOException {
        Path in = Paths.get("C:\\Users\\Matt\\Desktop\\corpora\\lob");
        Files.walkFileTree(in, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                List<String> input = Files.readAllLines(file, StandardCharsets.US_ASCII);
                List<String> output = new ArrayList<String>();
                for(String s : input){
                    if(s.length() >= 8)
                        s = s.substring(8);
                    s = s.replaceAll("[^A-Za-z ]", "");
                    s = s.trim();
                    s = s.toLowerCase();
                    output.add(s);
                }
                Path of = Paths.get(OUT.toString(), file.getFileName().toString());
                Files.createDirectories(of.getParent());
                Files.createFile(of);
                Files.write(of, output, StandardCharsets.UTF_8);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
