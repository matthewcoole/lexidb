package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.console.ANSICol;
import uk.ac.lancs.ucrel.ngram.NGram;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ngram extends Command {

    private Server s;
    private int n, pos = 0;
    private int page = 20;
    private uk.ac.lancs.ucrel.ops.Ngram ng;
    private boolean details;

    public Ngram(Server s) {
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

            int pos = (line.hasOption("sp")) ? Integer.parseInt(line.getOptionValue("s")) : this.pos;
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            int order = (line.hasOption("r")) ? -1 : 1;
            details = line.hasOption("d");

            ng.search(line.getArgs()[1], Integer.parseInt(line.getArgs()[2]), pos, page);

            System.out.println("\n" + ng.getLength() + " results for \"" + line.getArgs()[1] + "\" in " + ng.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<NGram> ngs = ng.it();
            print(ngs);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void print(List<NGram> ngrams){
        for(NGram ng : ngrams){
            ANSICol.generateCols(ng.getWords());
        }

        System.out.println("");
        ANSICol.printCols();
        System.out.println("\n");

        for(NGram ng : ngrams){
            System.out.println(ng.getCount() + "\t" + ANSICol.c(ng.getWords()));
        }
        System.out.println("");
    }
}
