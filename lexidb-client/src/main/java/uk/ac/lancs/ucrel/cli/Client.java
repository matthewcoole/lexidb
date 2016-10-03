package uk.ac.lancs.ucrel.cli;

import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.FileNameCompleter;
import jline.console.completer.StringsCompleter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import uk.ac.lancs.ucrel.cli.command.Command;
import uk.ac.lancs.ucrel.cli.command.ItCommand;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

public class Client {

    private ConsoleReader console;
    private Registry r;
    private Server s;
    private Map<String, Command> commands;
    private DefaultParser parser = new DefaultParser();
    private Command lastCommand;

    public Client(String host, int port) {
        try {
            r = LocateRegistry.getRegistry(host, port);
            Remote tmp = r.lookup("serv");
            if (tmp instanceof Server)
                s = (Server) tmp;
            commands = new HashMap<String, Command>();
            for (Command c : Command.getDefaultCommands(s, this)) {
                commands.put(c.getUsage().split(" ")[0], c);
            }
            console = getConsole();
        } catch (Exception e) {
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 1289;
        Client c = new Client(host, port);
        c.run();
    }

    public Command getLastCommand() {
        return lastCommand;
    }

    private ConsoleReader getConsole() {
        ConsoleReader c = null;
        try {
            c = new ConsoleReader();
            c.addCompleter(new ArgumentCompleter(new StringsCompleter(Command.getDefaultCommandsList()), new FileNameCompleter()));
            c.setPrompt("lexidb> ");
            c.clearScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void run() {
        String cmd = null;
        try {
            while ((cmd = console.readLine()) != null) {
                runCommand(cmd.trim());
                pause();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void runCommand(String cmd) {
        try {
            CommandLine line = parser.parse(new Options(), cmd.split(" "), true);
            String op = line.getArgs()[0];
            if (op.equals("clear")) {
                console.clearScreen();
                return;
            }
            if (commands.containsKey(op)) {
                Command c = commands.get(op);
                line = getCommandLine(c, cmd);
                if (line == null)
                    return;
                c.invoke(line);
                if (!(c instanceof ItCommand))
                    lastCommand = c;
            } else {
                System.err.println("Command not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Command failed!: " + e.getMessage());
        }
    }

    private CommandLine getCommandLine(Command c, String cmd) {
        try {
            return parser.parse(c.getOptions(), cmd.split(" "));
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            c.printHelp();
            return null;
        }
    }

    public Server getServer(){
        return s;
    }
}
