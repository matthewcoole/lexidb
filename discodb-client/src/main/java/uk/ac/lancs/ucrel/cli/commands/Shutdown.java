package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

public class Shutdown extends Command {

    private Server s;

    public Shutdown(Server s){
        super("shutdown", "Shutdown the server.");
        this.s = s;
    }

    public void invoke(CommandLine line){
        try {
            System.out.println("\nShutting down server...");
            s.shutdown();
            System.out.println("\nShutdown complete. Exiting...");
            System.exit(0);
        } catch (Exception e){
            this.setResult(new Result("Unable to shutdown server: " + e.getMessage()));
        }
    }
}
