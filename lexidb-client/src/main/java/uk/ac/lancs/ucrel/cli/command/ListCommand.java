package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.format.ANSIColourFormatter;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.ds.WordListEntry;
import uk.ac.lancs.ucrel.ops.ListOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class ListCommand extends Command {

    private Server s;
    private int page = 20;
    private ListOperation lop;
    private boolean details;

    public ListCommand(Server s) {
        super("list [TERM]", "List all words matching [TERM].");
        this.s = s;
        this.ops.addOption("h", "help", false, "display help information");
        this.ops.addOption("p", "pageSize", true, "set the page size in returned results - default " + page);
        this.ops.addOption("r", "reverse", false, "reverse the ordering of results");
        this.ops.addOption("d", "details", false, "display details i.e. tags");
    }

    public void invoke(CommandLine line) {
        try {
            lop = s.list();
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            boolean reverseOrder = line.hasOption("r");
            details = line.hasOption("d");

            lop.search(line.getArgs()[1], page, reverseOrder);

            System.out.println("\n" + lop.getLength() + " results for \"" + line.getArgs()[1] + "\" in " + lop.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void it() {
        try {
            List<WordListEntry> wordlist = lop.it();
            print(wordlist);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(List<WordListEntry> list) {
        List<Word> words = new ArrayList<Word>();
        ANSIColourFormatter ansi = new ANSIColourFormatter();
        for (WordListEntry col : list) {
            words.add(col.getWord());
        }
        ansi.generateCols(words);

        System.out.println("");
        ansi.printCols();
        System.out.println("\n");

        for (WordListEntry c : list) {
            System.out.println(c.getCount() + "\t" + ansi.c(c.getWord()));
        }
        System.out.println("");
    }
}
