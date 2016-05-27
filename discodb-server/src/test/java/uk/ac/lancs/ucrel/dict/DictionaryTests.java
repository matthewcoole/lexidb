package uk.ac.lancs.ucrel.dict;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Test;

public class DictionaryTests {

    private Dictionary d;

    @Before
    public void setup(){
        d = new Dictionary();
        d.put("test");
        d.put("dict");
        d.put("missing");
    }

    @Test
    public void testPut(){
        assertThat(d.put("test"), is(0));
        assertThat(d.put("dict"), is(1));
    }

    @Test
    public void testGet(){
        assertThat(d.get(0), is("test"));
        assertThat(d.get(1), is("dict"));
    }

    @Test
    public void testMap(){
        Dictionary d1 = new Dictionary();
        d1.put("dict");
        d1.put("test");
        int[] map = Dictionary.map(d, d1);
        int[] expected = {1, 0, -1};
        assertThat(map, is(expected));
    }

    @Test
    public void testSort(){
        Dictionary d1 = Dictionary.sort(d);
        assertThat(d1.get("dict"), is(0));
        assertThat(d1.get("missing"), is(1));
        assertThat(d1.get("test"), is(2));
    }

    @Test
    public void testCount(){
        d.put("dict");
        assertThat(d.count("test"), is(1));
        assertThat(d.count(1), is(2));
        assertThat(d.count(2), is(1));
    }
}
