package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Insert extends Command {

    private Server s;
    private InsertResult is;
    private int fileCount;

    public Insert(Server s){
        super("insert [PATH]", "Insert data from [PATH] into the database.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {

            Path dir = Paths.get(line.getArgs()[1]);

            fileCount = 0;

            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    s.sendRaw(file.getFileName().toString(), Files.readAllBytes(file));
                    fileCount++;
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("Transferred " + fileCount + " files to server for insertion");

            is = s.insert();

            while(!is.isComplete()){
                Thread.sleep(1000);
                is.print();
                is = s.lastInsert();
            }
            this.setResult(is);
        } catch (Exception e){
            this.setResult(new Result("\nUnable to insert data: " + e.getMessage()));
        }
    }
}
