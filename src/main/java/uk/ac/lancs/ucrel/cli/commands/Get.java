package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.cli.Param;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.util.Map;

public class Get extends Command {

    private Map<String, Param> props;

    public Get(Map<String, Param> props){
        super("get", "Display a search parameter. Leave param blank to see all.", "param");
        this.props = props;
        this.setParams(new String[0]);
    }

    public void invoke(){
        StringBuilder sb = new StringBuilder();
        if(this.getParams().length == 0){
            sb.append("\nSearch properties:\n\n");
            for(String prop: props.keySet()){
                Param p = props.get(prop);
                sb.append("\t").append(p.getName()).append(": ").append(" ").append(p.getValue()).append("\n\t\t").append(p.getDesc()).append("\n");
            }
        } else {
            sb.append("\n");
            for(String s : getParams()){
                sb.append(s).append(": ").append(this.props.get(s).getValue()).append("\n");
            }
        }
        this.setResult(new Result(sb.toString()));
    }
}
