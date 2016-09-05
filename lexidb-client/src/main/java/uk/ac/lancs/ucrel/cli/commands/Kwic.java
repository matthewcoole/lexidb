package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.console.ANSICol;
import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.Word;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Kwic extends Command {

    private Server s;
    private int context = 5;
    private int sortPos, limit, sortType = 0;
    private int page = 20;
    //private Map<String, Integer> printColors = new HashMap<String, Integer>();;
    private uk.ac.lancs.ucrel.ops.Kwic k;
    private boolean details;

    public Kwic(Server s) {
        super("kwic [TERM]", "Perform a keyword-in-context search for [TERM] ([TERM] may be a regular expression).");
        this.s = s;
        this.ops.addOption("h", "help", false, "display help information");
        this.ops.addOption("l", "limit", true, "limit results returned, 0 returns all results - default " + limit);
        this.ops.addOption("c", "context", true, "size of context window around keyword - default " + context);
        this.ops.addOption("s", "sortType", true, "sort type (1 = lexical sort, 2 = frequency sort)");
        this.ops.addOption("sp", "sortPosition", true, "position to sort on e.g. \"-1\" sorts on 1 word before the keyword");
        this.ops.addOption("p", "pageSize", true, "set the page size in returned results - default " + page);
        this.ops.addOption("r", "reverse", false, "reverse the ordering of results");
        this.ops.addOption("d", "details", false, "display details i.e. tags");
    }

    public void invoke(CommandLine line) {
        try {
            k = s.kwic();

            int context = (line.hasOption("c")) ? Integer.parseInt(line.getOptionValue("c")) : this.context;
            int limit = (line.hasOption("l")) ? Integer.parseInt(line.getOptionValue("l")) : this.limit;
            int sortType = (line.hasOption("s")) ? Integer.parseInt(line.getOptionValue("s")) : this.sortType;
            int sortPos = (line.hasOption("sp")) ? Integer.parseInt(line.getOptionValue("sp")) : this.sortPos;
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            int order = (line.hasOption("r")) ? -1 : 1;
            details = line.hasOption("d");

            k.search(line.getArgs()[1],
                    context,
                    limit,
                    sortType,
                    sortPos,
                    order,
                    page);

            System.out.println("\n" + k.getLength() + " results for \"" + line.getArgs()[1] + "\" in " + k.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<ConcordanceLine> lines = k.it();
            print(lines);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void print(List<ConcordanceLine> lines){
        ANSICol ansi = new ANSICol();

        for(ConcordanceLine l : lines){
            ansi.generateCols(l.getWords());
        }

        int preWordCount = (lines.get(0).getWords().size() - 1)/2;
        int longestPreLength = 0;
        for(ConcordanceLine l : lines){
            int length = 0;
            for(int i = 0; i < preWordCount; i++){
                length += l.getWords().get(i).toString().length();
            }
            if(length > longestPreLength)
                longestPreLength = length;
        }
        System.out.println("");

        ansi.printCols();
        System.out.println("\n");

        for(ConcordanceLine l : lines){
            System.out.print(getPadding(l, longestPreLength, preWordCount));
            for(Word w : l.getWords()){
                String word = ansi.c(w);
                System.out.print(word + " ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

    private String getPadding(ConcordanceLine l, int longestPreLength, int preWordCount){
        StringBuilder sb = new StringBuilder();
        int length = 0;
        for(int i =0; i < preWordCount; i++){
            length += l.getWords().get(i).toString().length();
        }
        int padding = longestPreLength - length;
        for(int j = 0; j < padding; j++){
            sb.append(" ");
        }
        return sb.toString();
    }
}
