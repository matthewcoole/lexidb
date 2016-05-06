package uk.ac.lancs.ucrel.cli.commands;

public class Exit extends Command {

    public Exit(){
        super("exit", "Exit the CLI application.");
    }

    public void invoke(){
        System.out.println("\nExiting...");
        System.exit(0);
    }
}
