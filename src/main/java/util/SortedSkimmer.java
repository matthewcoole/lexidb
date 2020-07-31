package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SortedSkimmer {

    private int[] pos, resetPoss;
    private char currentChar, resetChar;
    private int currentPos, resetPos;
    private List<int[]> lists = new ArrayList<>();
    private List<Character> chars = new ArrayList<>();

    public void add(int[] list, char c) {
        lists.add(list);
        chars.add(c);
    }

    public void restart(){
        pos = new int[lists.size()];
    }

    public void reset() {
        pos = resetPoss;
        currentChar = resetChar;
        resetPos = currentPos;
    }

    public void mark(){
        resetPoss = Arrays.copyOf(pos, pos.length);
        resetChar = currentChar;
        resetPos = currentPos;
    }

    public boolean next() {
        if (pos == null)
            restart();
        int lowestPos = Integer.MAX_VALUE;
        int pointer = -1;
        for (int i = 0; i < lists.size(); i++) {
            if (pos[i] < lists.get(i).length) {
                int j = lists.get(i)[pos[i]];
                if (j < lowestPos) {
                    lowestPos = j;
                    pointer = i;
                }
            }
        }
        if (pointer < 0)
            return false;
        currentPos = lowestPos;
        currentChar = chars.get(pointer);
        pos[pointer]++;
        return true;
    }

    public char getChar() {
        return currentChar;
    }

    public int getPos() {
        return currentPos;
    }

}
