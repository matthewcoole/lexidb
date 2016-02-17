package uk.ac.lancs.ucrel;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by Matt on 17/02/2016.
 */
public class ParserTest {
    private Parser p;

    @Before
    public void setup() throws IOException {
        p = new Parser(Paths.get("C:\\Users\\Matt\\Desktop\\data_dump"));
    }

    @Test
    public void testParseDir() throws IOException {
        p.parseDir(Paths.get("C:\\Users\\Matt\\Desktop\\LOB\\lob_text"));
    }
}
