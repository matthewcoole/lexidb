package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class It extends Command {

    private Server s;

    public It(Server s){
        super("it", "Iterates through the result set for the last search.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {
            //this.setResult(s.it());
        } catch (Exception e){
            this.setResult(new Result("Could not iterate results " + e.getMessage()));
        }
    }
}
