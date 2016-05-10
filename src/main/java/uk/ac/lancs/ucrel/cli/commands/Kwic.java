package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Kwic extends Command {

    private Server s;
    private int context = 5;
    private int limit, sortType, sortPos = 0;
    private int page = 20;

    public Kwic(Server s){
        super("kwic [TERM]", "Perform a keyword-in-context search for [TERM] ([TERM] may be a regular expression).");
        this.s = s;
        this.ops.addOption("h", "help", false, "display help information");
        this.ops.addOption("l", "limit", true, "limit results returned, 0 returns all results - default " + limit);
        this.ops.addOption("c", "context", true, "size of context window around keyword - default " + context);
        this.ops.addOption("s", "sortType", true, "sort type (1 = lexical sort, 2 = frequency sort)");
        this.ops.addOption("sp", "sortPosition", true, "position to sort on e.g. \"-1\" sorts on 1 word before the keyword");
        this.ops.addOption("p", "pageSize", true, "set the page size in returned results - default " + page);
        this.ops.addOption("r", "reverse", false, "reverse the ordering of results");
    }

    public void invoke(CommandLine line){
        if(line.hasOption("h")){
            this.printHelp();
            this.setResult(null);
            return;
        }
        try {
            int context = (line.hasOption("c"))? Integer.parseInt(line.getOptionValue("c")) : this.context;
            int limit = (line.hasOption("l"))? Integer.parseInt(line.getOptionValue("l")) : this.limit;
            int sortType = (line.hasOption("s"))? Integer.parseInt(line.getOptionValue("s")) : this.sortType;
            int sortPos = (line.hasOption("sp"))? Integer.parseInt(line.getOptionValue("sp")) : this.sortPos;
            int page = (line.hasOption("p"))? Integer.parseInt(line.getOptionValue("p")) : this.page;
            int order = (line.hasOption("r"))? -1 : 1;
            Result r = s.kwic(line.getArgs()[1],
                    context,
                    limit,
                    sortType,
                    sortPos,
                    order,
                    page);
            this.setResult(r);
        } catch (Exception e){
            this.setResult(new Result("kwic failed " + e.getMessage()));
        }
    }
}
