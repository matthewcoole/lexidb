package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Status extends Command {

    private Server s;

    public Status(Server s){
        super("status", "Displays the current status of the server.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {
            this.setResult(s.status());
        } catch (Exception e){
            this.setResult(new Result("Could not get status:  " + e.getMessage()));
        }
    }
}
