package uk.ac.lancs.ucrel;

import org.junit.Test;
import uk.ac.lancs.ucrel.rmi.Server;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RmiTestsIT {

    private Server s;

    @Test
    public void test(){
        try {
            Registry r = LocateRegistry.getRegistry(1289);
            Remote tmp = r.lookup("serv");
            if (tmp instanceof Server)
                s = (Server) tmp;

            Path dir = Paths.get("/home/mpc/bnc/");
            Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    s.sendRaw(file.getFileName().toString(), Files.readAllBytes(file));
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
