package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Insert extends Command {

    private Server s;

    public Insert(Server s){
        super("insert", "Insert data from the specified directory into the database", "dir");
        this.s = s;
    }

    public void invoke(){
        try {
            this.setResult(s.insert(this.getParams()[0]));
        } catch (Exception e){
            this.setResult(new Result("\nUnable to insert data: " + e.getMessage()));
        }
    }
}
