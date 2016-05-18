package uk.ac.lancs.ucrel.corpus;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertThat;

public class CorpusBuilderTests {

    @Test
    public void testCombineDictionaries(){
        List<String> dictA = Arrays.asList("this 2", "that 5", "time 1");
        List<String> dictB = Arrays.asList("this 1", "that 3", "up 3");
        List<String> dictC = Arrays.asList("and 2", "this 2", "that 3", "up 3");

        String[] expected = {"and 2", "that 11", "this 5", "time 1", "up 6"};

        List<String> result = CorpusBuilder.combineDictionaries(dictA, dictB, dictC);

        assertThat(result, IsIterableContainingInOrder.contains(expected));
    }
}
