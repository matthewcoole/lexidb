package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;

import java.util.List;

public class Help extends Command{

    private List<Command> commands;

    public Help(List<Command> commands){
        super("help", "Display help.");
        this.commands = commands;
    }

    public void invoke(CommandLine line){
        for(Command cmd : commands){
            cmd.printHelp();
        }
    }
}
