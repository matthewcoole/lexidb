package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.cli.Client;

public class ItCommand extends Command {

    private Client c;

    public ItCommand(Client c) {
        super("it", "Iterates through the result set for the last search.");
        this.c = c;
    }

    public void invoke(CommandLine line) {
        try {
            c.getLastCommand().it();
        } catch (Exception e) {
            System.err.println("Could not iterate results: " + e.getMessage());
        }
    }
}
