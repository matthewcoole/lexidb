package uk.ac.lancs.ucrel.cmd;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.ops.CollocateOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
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
            int cr = (line.hasOption("cr")) ? Integer.parseInt(line.getOptionValue("cr")) : this.cr;
            if (line.hasOption("c")) {
                cl = Integer.parseInt(line.getOptionValue("c"));
                cr = cl;
            }
            int page = (line.hasOption("p")) ? Integer.parseInt(line.getOptionValue("p")) : this.page;
            boolean reverseOrder = line.hasOption("r");
            details = line.hasOption("d");

            List<String> st = line.getArgList().subList(1, line.getArgList().size());

            String[] searchTerms = st.toArray(new String[0]);

            for (int i = 0; i < searchTerms.length; i++) {
                if (searchTerms[i].equals("\\null"))
                    searchTerms[i] = null;
            }

            col.search(searchTerms, cl, cr, page, reverseOrder);

            System.out.println("\n" + col.getLength() + " collocation results for \"" + Arrays.toString(searchTerms) + "\" retrieved in " + col.getTime() + "ms.\n");

            it();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
