package uk.ac.lancs.ucrel.conv;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class BNCConv {

    public static final SAXParserFactory factory = SAXParserFactory.newInstance();
    public static final Path OUT = Paths.get("/home/mpc/Desktop/bnc_tagged");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUT);
        Path in = Paths.get("/home/mpc/Desktop/BNC XML Edition");
        Files.walkFileTree(in,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    SAXParser saxParser = factory.newSAXParser();
                    BNCHandler handler = new BNCHandler(file.getFileName().toString());
                    saxParser.parse(file.toAbsolutePath().toString(), handler);
                    String filename = file.getFileName().toString();
                    filename = filename.substring(0, filename.lastIndexOf("."));
                    writeToFile(handler.getText(), filename);
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void writeToFile(String s, String file) throws IOException {
        if(!s.trim().equals("")) {
            Path of = Paths.get(OUT.toString(), file + ".tsv");
            List<String> ls = new ArrayList<String>();
            ls.add(s);
            Files.write(of, ls, StandardCharsets.UTF_8);
            System.out.println(of.toString());
        }
    }
}
