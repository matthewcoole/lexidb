package uk.ac.lancs.ucrel.region;

import org.junit.Test;
import uk.ac.lancs.ucrel.region.RegionBuilder;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class RegionBuilderTest {

    @Test
    public void testBuild() throws IOException {
        RegionBuilder rb = new RegionBuilder(Paths.get("C:\\Users\\Matt\\Desktop\\data_dump"));
        ArrayList<String> test = new ArrayList<String>();
        for(int i = 0; i < 1000000; i++){
            test.add("test" + i%10000);
        }
        rb.add(test);
        rb.build();
        rb.save();
    }
}
