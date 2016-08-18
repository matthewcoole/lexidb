package uk.ac.lancs.ucrel.result;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.rmi.result.KwicResult;
import uk.ac.lancs.ucrel.rmi.result.Result;
import uk.ac.lancs.ucrel.sort.FrequencyComparator;
import uk.ac.lancs.ucrel.sort.LexicalComparator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullKwicResult implements FullResult {
    private String searchTerm;
    private List<int[]> results;
    private int pageLength, position, regexMatches, sortType, sortPos, context, sortOrder;
    private long time;

    public FullKwicResult(String searchTerm, int regexMatches, int context, List<int[]> results){
        this.searchTerm = searchTerm;
        this.regexMatches = regexMatches;
        this.context = context;
        this.results = results;
    }

    public void setTime(long time){
        this.time = time;
    }

    public void setPageLength(int pageLength){
        this.pageLength = pageLength;
    }

    public List<int[]> getResults(){
        return results;
    }

    public void sort(int type, int pos, int order){
        this.sortType = type;
        this.sortPos = pos;
        this.sortOrder = order;
        if (sortType == 0)
            return;
        else if (sortType == 1) {
            Collections.sort(results, new LexicalComparator(context, sortPos));
        } else if (sortType == 2) {
            Collections.sort(results, new FrequencyComparator(context, sortPos, results));
        }
        if (order < 0)
            Collections.reverse(results);
    }

    public List<ConcordanceLine> it(CorpusAccessor ca){
        List<ConcordanceLine> lines = new ArrayList<ConcordanceLine>();
        for (int i = position; i < results.size() && i < (position + pageLength); i++) {
            lines.add(ca.getLine(results.get(i)));
        }
        position += pageLength;
        return lines;
    }
}
