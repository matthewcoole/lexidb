package uk.ac.lancs.ucrel.dict;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class DictionaryTests {

    private Dictionary d;

    @Before
    public void setup() {
        d = new Dictionary();
        d.put("a \tAT0\tART\ta");
        d.put("to \tPRP\tPREP");
        d.put("and \tCJC\tCONJ\tand");
    }

    @Test
    public void testGetString(){
        d.put("the \tAT0\tART\tthe");
        Integer[] expected = {0, 3};
        d.loadIndexTrees();
        assertThat(d.get("AT0", 1), is(expected));
    }

    @Test
    public void testPut() {
        assertThat(d.put("a \tAT0\tART\ta"), is(0));
        assertThat(d.put("to \tPRP\tPREP"), is(1));
    }

    @Test
    public void testGet() {
        assertThat(d.get(0), is("a\tAT0\tART\ta"));
        assertThat(d.get(1), is("to\tPRP\tPREP"));
    }

    @Test
    public void testMap() {
        Dictionary d1 = new Dictionary();
        d1.put("to \tPRP\tPREP");
        d1.put("a \tAT0\tART\ta");
        int[] map = Dictionary.map(d, d1);
        int[] expected = {1, 0, -1};
        assertThat(map, is(expected));
    }

    @Test
    public void testSort() {
        Dictionary d1 = Dictionary.sort(d);
        assertThat(d1.get("a \tAT0\tART\ta"), is(0));
        assertThat(d1.get("and \tCJC\tCONJ\tand"), is(1));
        assertThat(d1.get("to \tPRP\tPREP"), is(2));
    }

    @Test
    public void testCount() {
        d.put("to \tPRP\tPREP");
        assertThat(d.count("a \tAT0\tART\ta"), is(1));
        assertThat(d.count("to \tPRP\tPREP"), is(2));
        assertThat(d.count(2), is(1));
    }

    @Test
    public void testGetWord() {
        assertThat(d.getWords("a").get(0), is(0));
    }
}
