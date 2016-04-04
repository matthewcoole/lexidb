package uk.ac.lancs.ucrel.region;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class RegionAccessorTest {

    @Test
    public void testSearch() throws IOException {
        RegionAccessor ra = new RegionAccessor(Paths.get("C:\\Users\\Matt\\Desktop\\data_dump\\0000"));
        long start = System.currentTimeMillis();
        ra.search("aaab", 10);
        long end = System.currentTimeMillis();
        System.out.println((end - start) + "ms");
    }
}
