package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;

public class ShutdownCommand extends Command {

    private Server s;

    public ShutdownCommand(Server s) {
        super("shutdown", "Shutdown the server.");
        this.s = s;
    }

    public void invoke(CommandLine line) {
        try {
            System.out.println("\nShutting down server...");
            s.shutdown();
            System.out.println("\nShutdown complete. Exiting...");
            System.exit(0);
        } catch (Exception e) {
            System.err.println("Unable to shutdown server: " + e.getMessage());
        }
    }
}
