package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.format.ANSIColourFormatter;
import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.ops.CollocateOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ColCommand extends Command {

    private Server s;
    private int cl = 2, cr = 2, pos = 0;
    private int page = 20;
    private CollocateOperation col;
    private boolean details;

    public ColCommand(Server s) {
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
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            boolean reverseOrder = line.hasOption("r");
            details = line.hasOption("d");

            col.search(line.getArgs()[1], cl, cr, page, reverseOrder);

            System.out.println("\n" + col.getLength() + " results for \"" + line.getArgs()[1] + "\" in " + col.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<Collocate> cols = col.it();
            print(cols);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(List<Collocate> cols) {
        List<Word> words = new ArrayList<Word>();
        ANSIColourFormatter ansi = new ANSIColourFormatter();
        for (Collocate col : cols) {
            words.add(col.getWord());
        }
        ansi.generateCols(words);

        System.out.println("");
        ansi.printCols();
        System.out.println("\n");

        for (Collocate c : cols) {
            System.out.println(c.getCount() + "\t" + ansi.c(c.getWord()));
        }
        System.out.println("");
    }
}