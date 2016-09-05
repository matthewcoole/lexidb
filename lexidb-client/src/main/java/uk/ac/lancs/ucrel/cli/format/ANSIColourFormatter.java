package uk.ac.lancs.ucrel.cli.format;

import uk.ac.lancs.ucrel.ds.Word;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ANSIColourFormatter {

    public static int RED = 9, GREEN = 34, YELLOW = 220, BLUE = 19, AQUA = 38, PURPLE = 129, BROWN = 130, GREY = 246;
    private static Random r;
    private Map<String, Integer> cols = new HashMap<String, Integer>();

    /**
     * Builds a string surrounded by appropriate ANSI escape characters for the specified color.
     */
    public static String c(String s, int col) {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[38;5;");
        sb.append(col);
        sb.append("m");
        sb.append(s);
        sb.append("\033[0;00m");
        return sb.toString();
    }

    /**
     * Builds a string with ANSI escape characters for a random color.
     */
    public static String rc(String s) {
        if (r == null)
            r = new Random();
        return c(s, r.nextInt(256));
    }

    public void resetCols() {
        cols = new HashMap<String, Integer>();
    }

    public void printCols() {
        for (String pos : cols.keySet()) {
            System.out.print(ANSIColourFormatter.c(pos, cols.get(pos)));
            System.out.print(" ");
        }
    }

    public String c(Word w) {
        StringBuilder sb = new StringBuilder();
        if (w.getTags() != null && w.getTags().size() >= 2) {
            String pos = w.getTags().get(1);
            sb.append(c(w.toString(), cols.get(pos)));
        } else {
            sb.append(w.toString());
        }
        return sb.toString();
    }

    public String c(Collection<Word> words) {
        StringBuilder sb = new StringBuilder();
        for (Word w : words) {
            sb.append(c(w));
            sb.append(" ");
        }
        return sb.toString();
    }

    public void generateCols(Collection<Word> words) {
        for (Word w : words) {
            if (w.getTags() != null && w.getTags().size() >= 2) {
                String pos = w.getTags().get(1);
                if (!cols.containsKey(pos)) {
                    cols.put(pos, new Random().nextInt(256));
                }
            }
        }
    }
}
