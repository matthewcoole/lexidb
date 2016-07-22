package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.Client;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class It extends Command {

    private Client c;

    public It(Client c){
        super("it", "Iterates through the result set for the last search.");
        this.c = c;
    }

    public void invoke(CommandLine line){
        try {
            c.getLastCommand().it();
        } catch (Exception e){
            this.setResult(new Result("Could not iterate results " + e.getMessage()));
        }
    }
}
