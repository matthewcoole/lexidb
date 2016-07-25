package uk.ac.lancs.ucrel.cli.console;

import java.util.Random;

public class ANSICol {

    private static Random r;

    public static int RED = 9, GREEN = 34, YELLOW =220, BLUE = 19, AQUA = 38, PURPLE = 129, BROWN = 130, GREY = 246;

    /**
     * Builds a string surrounded by appropriate ANSI escape characters for the specified color.
     */
    public static String c(String s, int col){
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
    public static String rc(String s){
        if(r == null)
            r = new Random();
        return c(s, r.nextInt(256));
    }
}
