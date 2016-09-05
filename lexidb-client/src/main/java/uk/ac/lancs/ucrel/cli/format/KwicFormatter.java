package uk.ac.lancs.ucrel.cli.format;

import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.ds.Word;

import java.util.List;

public class KwicFormatter {

    private int preWordCount, longestPreLength, longestKeyword;

    public KwicFormatter(List<Kwic> lines) {
        preWordCount = (lines.get(0).getWords().size() - 1) / 2;
        for (Kwic l : lines) {
            int length = 0;
            for (int i = 0; i < preWordCount; i++) {
                length += l.getWords().get(i).toString().length();
            }
            if (length > longestPreLength)
                longestPreLength = length;
            int keywordLength = l.getWords().get(preWordCount).toString().length();
            if (keywordLength > longestKeyword)
                longestKeyword = keywordLength;
        }
    }

    public String pad(Kwic l, ANSIColourFormatter ansi) {
        StringBuilder sb = new StringBuilder();
        sb.append(getLinePadding(l));
        for (int i = 0; i < preWordCount; i++) {
            sb.append(ansi.c(l.getWords().get(i))).append(' ');
        }
        sb.append(getPaddedKeyword(l.getWords().get(preWordCount), ansi));
        for (int i = preWordCount + 1; i < (preWordCount * 2) + 1; i++) {
            sb.append(ansi.c(l.getWords().get(i))).append(' ');
        }
        return sb.toString();
    }

    private String getLinePadding(Kwic l) {
        StringBuilder sb = new StringBuilder();
        int length = 0;
        for (int i = 0; i < preWordCount; i++) {
            length += l.getWords().get(i).toString().length();
        }
        int padding = longestPreLength - length;
        for (int j = 0; j < padding; j++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String getPaddedKeyword(Word w, ANSIColourFormatter ansi) {
        StringBuilder sb = new StringBuilder();
        int pad = (longestKeyword + 4 - w.toString().length()) / 2;
        int length = 0;
        for (int i = 1; i < pad; i++) {
            sb.append(' ');
            length++;
        }
        sb.append(ansi.c(w));
        length += w.toString().length();
        while (length < (longestKeyword + 3)) {
            sb.append(' ');
            length++;
        }
        return sb.toString();
    }

}
