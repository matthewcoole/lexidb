package uk.ac.lancs.ucrel;

import org.junit.Test;
import uk.ac.lancs.ucrel.region.RegionBuilder;

import java.nio.file.Paths;
import java.util.Arrays;

public class RegionBuilderTest {

    @Test
    public void testBuild(){
        RegionBuilder rb = new RegionBuilder(Paths.get("C:\\Users\\Matt\\Desktop\\data_dump"));
        rb.add(Arrays.asList("this", "is", "a", "test", "list", "of", "strings", "to", "test", "the", "builder"));
        rb.build();
    }
}
