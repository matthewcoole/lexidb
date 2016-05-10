package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;

public class Exit extends Command {

    public Exit(){
        super("exit", "Exit the CLI application.");
    }

    public void invoke(CommandLine line){
        System.out.println("\nExiting...");
        System.exit(0);
    }
}
