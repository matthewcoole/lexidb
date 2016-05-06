package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.rmi.result.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Help extends Command{

    private Map<String, Command> commands;

    public Help(Map<String, Command> commands){
        super("help", "Display help.");
        this.commands = commands;
    }

    public void invoke(){
        StringBuilder sb = new StringBuilder("\ndiscoDB commands:\n\n");
        List<String> orderedCommands = new ArrayList<String>(commands.keySet());
        Collections.sort(orderedCommands);
        for(String cmd : orderedCommands){
            Command c = commands.get(cmd);
            sb.append(helpText(c.getName(), c.getDesc(), c.getParams()));
        }
        this.setResult(new Result(sb.toString()));
    }

    private String helpText(String command, String description, String... opts){
        StringBuilder sb = new StringBuilder("\t");
        sb.append(command).append(" ");
        for(String op : opts){
            sb.append("[").append(op).append("]");
        }
        sb.append("\n\t\t").append(description).append("\n\n");
        return sb.toString();
    }
}
