package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.format.ANSIColourFormatter;
import uk.ac.lancs.ucrel.cli.format.KwicFormatter;
import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.ops.KwicOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public class KwicCommand extends Command {

    private Server s;
    private int context = 5;
    private int sortPos, limit, sortType = 0;
    private int page = 20;
    private KwicOperation k;
    private boolean details;

    public KwicCommand(Server s) {
        super("kwic [TERM] [TAGS]...", "Perform a keyword-in-context search for [TERM] /w [TAGS]... ([TERM] & [TAGS] may be regular expressions).");
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
            boolean reverse = line.hasOption("r");
            details = line.hasOption("d");

            List<String> st = line.getArgList().subList(1, line.getArgList().size());

            String[] searchTerms = st.toArray(new String[0]);

            for (int i = 0; i < searchTerms.length; i++) {
                if (searchTerms[i].equals("\\null"))
                    searchTerms[i] = null;
            }

            k.search(searchTerms,
                    context,
                    limit,
                    sortType,
                    sortPos,
                    reverse,
                    page);

            int kLength = k.getLength();

            System.out.println("\n" + kLength + " concordance lines for \"" + Arrays.toString(searchTerms) + "\" retrieved in " + k.getTime() + "ms.\n");

            if(kLength > 0)
                it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<Kwic> lines = k.it();
            print(lines, details);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void print(List<Kwic> lines, boolean details) {
        if (lines.size() == 0)
            return;

        ANSIColourFormatter ansi = new ANSIColourFormatter();

        for (Kwic l : lines) {
            ansi.generateCols(l.getWords());
        }

        KwicFormatter align = new KwicFormatter(lines, details);

        System.out.println("");
        ansi.printCols();
        System.out.println("\n");

        for (Kwic l : lines) {
            System.out.println(align.pad(l, ansi));
        }
        System.out.println();
    }
}
