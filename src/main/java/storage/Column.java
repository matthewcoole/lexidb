package storage;

import java.nio.file.Path;

public class Column {
    public String name, xml, clean;
    private Path path;

    public void setPath(Path p) {
        this.path = p;
    }
}
