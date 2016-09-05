package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.Word;
import uk.ac.lancs.ucrel.cli.console.ANSICol;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class Collocate extends Command {

    private Server s;
    private int cl = 2, cr = 2, pos = 0;
    private int page = 20;
    private uk.ac.lancs.ucrel.ops.Collocate col;
    private boolean details;

    public Collocate(Server s) {
        super("col [TERM]", "Perform a search for collocates of [TERM].");
        this.s = s;
        this.ops.addOption("h", "help", false, "display help information");
        this.ops.addOption("c", "context", true, "how far to the left/right to of the search term to look for collocates");
        this.ops.addOption("cl", "contextLeft", true, "how far to the left of the search term to search for collocates");
        this.ops.addOption("cr", "contextLeft", true, "how far to the right of the search term to search for collocates");
        this.ops.addOption("p", "pageSize", true, "set the page size in returned results - default " + page);
        this.ops.addOption("r", "reverse", false, "reverse the ordering of results");
        this.ops.addOption("d", "details", false, "display details i.e. tags");
    }

    public void invoke(CommandLine line) {
        try {
            col = s.collocate();
            int cl = (line.hasOption("cl")) ? Integer.parseInt(line.getOptionValue("cl")) : this.cl;
            int cr = (line.hasOption("cr")) ? Integer.parseInt(line.getOptionValue("cr")) : this.pos;
            if (line.hasOption("c")) {
                cl = Integer.parseInt(line.getOptionValue("c"));
                cr = cl;
            }
            int pos = (line.hasOption("sp")) ? Integer.parseInt(line.getOptionValue("s")) : this.pos;
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            int order = (line.hasOption("r")) ? -1 : 1;
            details = line.hasOption("d");

            col.search(line.getArgs()[1], cl, cr, page);

            System.out.println("\n" + col.getLength() + " results for \"" + line.getArgs()[1] + "\" in " + col.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<uk.ac.lancs.ucrel.col.Collocate> cols = col.it();
            print(cols);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(List<uk.ac.lancs.ucrel.col.Collocate> cols) {
        List<Word> words = new ArrayList<Word>();
        ANSICol ansi = new ANSICol();
        for (uk.ac.lancs.ucrel.col.Collocate col : cols) {
            words.add(col.getWord());
        }
        ansi.generateCols(words);

        System.out.println("");
        ansi.printCols();
        System.out.println("\n");

        for (uk.ac.lancs.ucrel.col.Collocate c : cols) {
            System.out.println(c.getCount() + "\t" + ansi.c(c.getWord()));
        }
        System.out.println("");
    }
}
