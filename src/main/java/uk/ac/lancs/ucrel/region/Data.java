package uk.ac.lancs.ucrel.region;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Data {

    private static final int BUF_SIZE = 1024 * 4;

    private Path regionPath, dataPath;
    private DataOutputStream dos;
    private DataInputStream dis;

    public Data(String name, Path regionPath) throws IOException {
        this.regionPath = regionPath;
        dataPath = Paths.get(regionPath.toString(), name);
        Files.deleteIfExists(dataPath);
        Files.createFile(dataPath);
    }

    public void add(Integer i) throws IOException {
        getOutputStream().writeInt(i);
    }

    public void save() throws IOException {
        getOutputStream().flush();
        getOutputStream().close();
        dos = null;
    }

    public boolean hasMore() throws IOException {
        return getInputStream().available() > 0;
    }

    public int getInt() throws IOException {
        return getInputStream().readInt();
    }

    private DataInputStream getInputStream() throws IOException {
        if(dis == null)
            dis = new DataInputStream(new BufferedInputStream(Files.newInputStream(dataPath), BUF_SIZE));
        return dis;
    }

    private DataOutputStream getOutputStream() throws IOException {
        if(dos == null)
            dos = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(dataPath), BUF_SIZE));
        return dos;
    }
}
