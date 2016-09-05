package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;

import java.util.List;

public class HelpCommand extends Command {

    private List<Command> commands;

    public HelpCommand(List<Command> commands) {
        super("help", "Display help.");
        this.commands = commands;
    }

    public void invoke(CommandLine line) {
        for (Command cmd : commands) {
            cmd.printHelp();
        }
    }
}
