package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.format.ANSIColourFormatter;
import uk.ac.lancs.ucrel.ds.Ngram;
import uk.ac.lancs.ucrel.ops.NgramOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

public class NgramCommand extends Command {

    private Server s;
    private int pos = 0;
    private int page = 20;
    private NgramOperation ng;
    private boolean details;

    public NgramCommand(Server s) {
        super("ngram [TERM] [N]", "Perform an ngram search search for [TERM] for [N]-grams ([TERM] may be a regular expression).");
        this.s = s;
        this.ops.addOption("h", "help", false, "display help information");
        this.ops.addOption("sp", "searchPosition", true, "position to of search term in ngram");
        this.ops.addOption("p", "pageSize", true, "set the page size in returned results - default " + page);
        this.ops.addOption("r", "reverse", false, "reverse the ordering of results");
        this.ops.addOption("d", "details", false, "display details i.e. tags");
    }

    public void invoke(CommandLine line) {
        try {
            ng = s.ngram();

            int pos = (line.hasOption("sp")) ? Integer.parseInt(line.getOptionValue("sp")) : this.pos;
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            boolean reverse = line.hasOption("r");
            details = line.hasOption("d");

            List<String> st = line.getArgList().subList(1, line.getArgList().size());

            int n = Integer.parseInt(st.get(st.size() - 1));

            st = st.subList(0, st.size() - 1);

            String[] searchTerms = st.toArray(new String[0]);

            for(int i = 0; i < searchTerms.length; i++){
                if(searchTerms[i].equals("\\null"))
                    searchTerms[i] = null;
            }

            ng.search(searchTerms, n, pos, page, reverse);

            System.out.println("\n" + ng.getLength() + " " + n + "grams for \"" + Arrays.toString(searchTerms) + "\" retrieved in " + ng.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<Ngram> ngs = ng.it();
            print(ngs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(List<Ngram> ngrams) {
        ANSIColourFormatter ansi = new ANSIColourFormatter();
        for (Ngram ng : ngrams) {
            ansi.generateCols(ng.getWords());
        }

        System.out.println("");
        ansi.printCols();
        System.out.println("\n");

        for (Ngram ng : ngrams) {
            System.out.println(ng.getCount() + "\t" + ansi.c(ng.getWords()));
        }
        System.out.println("");
    }
}
