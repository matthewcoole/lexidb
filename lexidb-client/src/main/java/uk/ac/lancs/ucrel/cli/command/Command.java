package uk.ac.lancs.ucrel.cli.command;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import uk.ac.lancs.ucrel.cli.Client;
import uk.ac.lancs.ucrel.rmi.Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Command implements Comparable<Command> {

    private static List<Command> defaultCommands;

    protected String usage;
    protected String desc;
    protected Options ops;

    public Command(String usage) {
        this.usage = usage;
    }

    public Command(String usage, String desc) {
        this.usage = usage;
        this.desc = desc;
        this.ops = new Options();
    }

    public static List<Command> getDefaultCommands(Server s, Client c) {
        defaultCommands = new ArrayList<Command>();
        defaultCommands.add(new HelpCommand(defaultCommands));
        defaultCommands.add(new ShutdownCommand(s));
        defaultCommands.add(new ExitCommand());
        defaultCommands.add(new InsertCommand(s));
        defaultCommands.add(new KwicCommand(s));
        defaultCommands.add(new KwicCommand(s));
        defaultCommands.add(new NgramCommand(s));
        defaultCommands.add(new ColCommand(s));
        defaultCommands.add(new ListCommand(s));
        defaultCommands.add(new ItCommand(c));
        defaultCommands.add(new StatusCommand(s));
        Collections.sort(defaultCommands);
        return defaultCommands;
    }

    public static List<String> getDefaultCommandsList() {
        List<String> cmdNames = new ArrayList<String>();
        for (Command cmd : defaultCommands) {
            cmdNames.add(cmd.usage.split(" ")[0]);
        }
        return cmdNames;
    }

    public Options getOptions() {
        return ops;
    }

    public void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(usage, desc, ops, null);
    }

    public void invoke(CommandLine line) {
        System.err.println("Unimplemented command!");
    }

    public void it() {
        System.err.println("Cannot iterate!");
    }

    public int compareTo(Command cmd) {
        return this.usage.compareTo(cmd.usage);
    }

    public String getUsage() {
        return usage;
    }
}
