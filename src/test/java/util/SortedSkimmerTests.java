package util;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SortedSkimmerTests {

    @Test
    public void test() {
        int[] a = {0, 1, 4, 7, 9};
        int[] b = {2, 3, 4, 5, 8, 9, 10};
        SortedSkimmer sk = new SortedSkimmer();
        sk.add(a, 'a');
        sk.add(b, 'b');

        int[] expectedPos = {0, 1, 2, 3, 4, 4, 5, 7, 8, 9, 9, 10};
        char[] expectedChar = {'a', 'a', 'b', 'b', 'a', 'b', 'b', 'a', 'b', 'a', 'b', 'b'};

        int i = 0;
        while (sk.next()) {
            assertThat(sk.getPos(), is(expectedPos[i]));
            assertThat(sk.getChar(), is(expectedChar[i]));
            i++;
        }
    }
}
