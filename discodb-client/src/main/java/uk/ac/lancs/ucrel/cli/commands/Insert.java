package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.InsertResult;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Insert extends Command {

    private Server s;
    private InsertResult is;

    public Insert(Server s){
        super("insert [PATH]", "Insert data from [PATH] into the database.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {
            is = s.insert(line.getArgs()[1]);
            while(!is.isComplete()){
                Thread.sleep(1000);
                is.print();
                is = s.lastInsert();
            }
            this.setResult(is);
        } catch (Exception e){
            this.setResult(new Result("\nUnable to insert data: " + e.getMessage()));
        }
    }
}
