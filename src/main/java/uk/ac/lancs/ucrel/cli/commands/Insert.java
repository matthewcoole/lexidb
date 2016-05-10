package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Insert extends Command {

    private Server s;

    public Insert(Server s){
        super("insert [PATH]", "Insert data from [PATH] into the database.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {
            this.setResult(s.insert(line.getArgs()[1]));
        } catch (Exception e){
            this.setResult(new Result("\nUnable to insert data: " + e.getMessage()));
        }
    }
}
