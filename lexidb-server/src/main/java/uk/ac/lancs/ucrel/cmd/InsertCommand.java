package uk.ac.lancs.ucrel.cmd;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.ops.InsertOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class InsertCommand extends Command {

    private Server s;
    private InsertOperation i;

    public InsertCommand(Server s) {
        super("insert [PATH]", "Insert data from [PATH] into the database.");
        this.s = s;
    }

    public void invoke(CommandLine line) {
        try {

            Path dir = Paths.get(line.getArgs()[1]);

            i = s.insert();

            Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    i.sendRaw(file.getFileName().toString(), Files.readAllBytes(file));
                    return FileVisitResult.CONTINUE;
                }
            });

            i.insert();

            while (!i.isComplete()) {
                Thread.sleep(1000);
            }

            System.out.println("\nInserted " + i.getFileCount() + " files in " + i.getTime() + "ms.\n");

        } catch (Exception e) {
            System.err.println("Unable to insert data: " + e.getMessage());
        }
    }
}
