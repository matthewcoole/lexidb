package util;

import java.util.Arrays;

public class ArrayUtils {
    public static int[] intersectSortedArrays(int[] a, int[] b) {
        int length = a.length > b.length ? b.length : a.length;
        int[] temp = new int[length];
        int i = 0, j = 0, resultSize = 0;
        while (i < a.length && j < b.length) {
            int aVal = a[i];
            int bVal = b[j];
            if (aVal == bVal) {
                temp[resultSize++] = aVal;
                i++;
                j++;
                continue;
            } else if (aVal > bVal) {
                j++;
                continue;
            } else
                i++;
        }
        return Arrays.copyOf(temp, resultSize);
    }
}
