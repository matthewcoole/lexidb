package uk.ac.lancs.ucrel.parser;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class TextParserTest {

    @Test
    public void test() throws IOException {
        TextParser tp = new TextParser(Paths.get("C:\\Users\\Matt\\Desktop\\lob_test"));
        tp.parse(Paths.get("C:\\Users\\Matt\\Desktop\\corpora\\lob"));
    }
}