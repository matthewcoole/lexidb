package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;

public class ExitCommand extends Command {

    public ExitCommand() {
        super("exit", "Exit the CLI application.");
    }

    public void invoke(CommandLine line) {
        System.out.println("\nExiting...");
        System.exit(0);
    }
}
