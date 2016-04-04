package uk.ac.lancs.ucrel.parser;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class TextParserTest {

    @Test
    public void test() throws IOException {
        TextParser tp = new TextParser(Paths.get("C:\\Users\\Matt\\Desktop\\bnc_4_node"));
        tp.parse(Paths.get("C:\\Users\\Matt\\Desktop\\h_small\\A\\00"));
    }
}
