package uk.ac.lancs.ucrel.file.system;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class FileUtilsIT {

    @ClassRule
    public static TemporaryFolder DATA = new TemporaryFolder();


    @Test
    public void testReadAndWrite() throws IOException {
        Path fp = Paths.get(DATA.getRoot().getPath(), "test.db");

        int[] ints = generateRandomNumbers();

        long start = System.currentTimeMillis();
        FileUtils.write(fp, ints);
        long mid = System.currentTimeMillis();
        IntBuffer ib = FileUtils.readAllInts(fp);
        int count = 0;
        while (ib.hasRemaining()) {
            assertEquals(ints[count], ib.get());
            count++;
        }
        long end = System.currentTimeMillis();
        System.out.println(ints.length + " ints - Write: " + (mid - start) + "ms, Read: " + (end - mid) + "ms");
    }

    private int[] generateRandomNumbers() {
        int size = 2000000;
        int[] ints = new int[size];
        Random r = new Random();

        for (int i = 0; i < size; i++) {
            ints[i] = r.nextInt(100);
        }
        return ints;
    }
}
