package uk.ac.lancs.ucrel.corpus;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CorpusBuilderTest {

    private List<String> randomWordList(){
        List<String> words = new ArrayList<String>();
        Random rn = new Random();
        for(int i = 0; i < 1000000; i++){
            int wordLength = rn.nextInt(4) + 1;
            StringBuilder sb = new StringBuilder();
            for(int n = 0; n < wordLength; n++){
                char c = (char)(rn.nextInt(26) + 97);
                sb.append(c);
            }
            words.add(sb.toString());
        }
        return words;
    }

    @Test
    public void test() throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        CorpusBuilder cb = new CorpusBuilder(Paths.get("C:\\Users\\Matt\\Desktop\\data_dump"));
        for(int i = 0; i < 10; i++){
            cb.addRegion(randomWordList());
        }
        long end = System.currentTimeMillis();
        System.out.println("Corpus built in " + (end-start) + "ms");
    }
}
