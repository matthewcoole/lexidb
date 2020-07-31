package util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class ArrayUtilsTests {

    @Test
    public void testIntersectSortedArrays() {
        int[] a = {1, 3, 5, 7, 8, 9};
        int[] b = {3, 4, 5, 6, 7, 9};
        int[] result = ArrayUtils.intersectSortedArrays(a, b);
        int[] expected = {3, 5, 7, 9};
        assertArrayEquals(expected, result);
    }
}
