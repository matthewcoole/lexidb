package uk.ac.lancs.ucrel.cli.commands;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import uk.ac.lancs.ucrel.cli.Client;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class Command implements Comparable<Command> {

    private static List<Command> defaultCommands;

    protected String usage;
    protected String desc;
    protected String[] params;
    protected Result result;
    protected Options ops;

    public Command(String usage){
        this.usage = usage;
    }

    public Command(String usage, String desc){
        this.usage = usage;
        this.desc = desc;
        this.ops = new Options();
    }

    public static List<Command> getDefaultCommands(Server s, Client c){
        defaultCommands = new ArrayList<Command>();
        defaultCommands.add(new Help(defaultCommands));
        defaultCommands.add(new Shutdown(s));
        defaultCommands.add(new Exit());
        defaultCommands.add(new Insert(s));
        defaultCommands.add(new Kwic(s));
        defaultCommands.add(new Ngram(s));
        defaultCommands.add(new Collocate(s));
        defaultCommands.add(new It(c));
        defaultCommands.add(new Status(s));
        Collections.sort(defaultCommands);
        return defaultCommands;
    }

    public static List<String> getDefaultCommandsList(){
        List<String> cmdNames = new ArrayList<String>();
        for(Command cmd : defaultCommands){
            cmdNames.add(cmd.usage.split(" ")[0]);
        }
        return cmdNames;
    }

    public Options getOptions(){
        return ops;
    }

    public void printHelp(){
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp(usage, desc, ops, null);
    }

    public Result getResult(){
        if(result == null)
            return new Result("");
        return result;
    }

    public void setResult(Result r){
        this.result = r;
    }

    public void invoke(CommandLine line){
        System.err.println("Unimplemented command!");
    }

    public void it(){
        System.err.println("Cannot iterate!");
    }

    public int compareTo(Command cmd){
        return this.usage.compareTo(cmd.usage);
    }

    public String getUsage(){
        return usage;
    }
}
