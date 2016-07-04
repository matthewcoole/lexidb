package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class Insert extends Command {

    private Server s;
    private uk.ac.lancs.ucrel.ops.Insert i;

    public Insert(Server s){
        super("insert [PATH]", "Insert data from [PATH] into the database.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {

            Path dir = Paths.get(line.getArgs()[1]);

            i = s.insert();

            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    i.sendRaw(file.getFileName().toString(), Files.readAllBytes(file));
                    return FileVisitResult.CONTINUE;
                }
            });

            i.insert();

            while(!i.isComplete()){
                Thread.sleep(1000);
            }

            System.out.println("Insert finished!");

            /*
            System.out.println("Transferred " + fileCount + " files to server for insertion");

            s.distributeRaw();

            System.out.println("Files distributed between all servers");

            is = s.insert();

            while(!is.isComplete()){
                Thread.sleep(1000);
                System.out.println(is.status());
                is = s.lastInsert();
            }

            s.refresh();
*/
            //this.setResult(null);
        } catch (Exception e){
            this.setResult(new Result("\nUnable to insert data: " + e.getMessage()));
        }
    }
}
