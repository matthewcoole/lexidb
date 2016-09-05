package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;

public class StatusCommand extends Command {

    private Server s;

    public StatusCommand(Server s) {
        super("status", "Displays the current status of the server.");
        this.s = s;
    }

    public void invoke(CommandLine line) {
        try {
            System.out.println("Not implemented!");
        } catch (Exception e) {
            System.err.println("Unable to get status: " + e.getMessage());
        }
    }
}
