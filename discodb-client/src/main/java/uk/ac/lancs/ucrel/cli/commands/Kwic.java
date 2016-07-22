package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.conc.Word;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kwic extends Command {

    private Server s;
    private int context = 5;
    private int limit, sortType, sortPos = 0;
    private int page = 20;
    private Map<String, Ansi.Color> printColors = new HashMap<String, Ansi.Color>();
    private Ansi.Color[] cs = Ansi.Color.values();
    private int currentPrintColor = 0;
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
        for(ConcordanceLine l : lines){
            for(Word w : l.getWords()){
                String pos = w.getTags().get(1);
                if(!printColors.containsKey(pos)){
                    printColors.put(pos, cs[currentPrintColor]);
                    currentPrintColor = (currentPrintColor +1) % cs.length;
                }
            }
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

        AnsiConsole.systemInstall();
        System.out.println("");
        for(String pos : printColors.keySet()){
            System.out.print(Ansi.ansi().fg(printColors.get(pos)).a(pos).reset());
            System.out.print(" ");
        }
        System.out.println("\n");
        for(ConcordanceLine l : lines){
            System.out.print(getPadding(l, longestPreLength, preWordCount));
            for(Word w : l.getWords()){
                String pos = w.getTags().get(1);
                String word = details ? w.details() : w.toString();
                System.out.print(Ansi.ansi().fg(printColors.get(pos)).a(word).reset());
                System.out.print(" ");
            }
            System.out.println("");
        }
        System.out.println("");
        AnsiConsole.systemUninstall();
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
