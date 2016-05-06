package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.cli.Param;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.util.Map;

public class Kwic extends Command {

    private Server s;
    private Map<String, Param> props;

    public Kwic(Server s, Map<String, Param> props){
        super("kwic", "Perform a keyword-in-context search for the specified keyword", "keyword");
        this.s = s;
        this.props = props;
    }

    public void invoke(){
        try {
            Result r = s.kwic(this.getParams()[0], this.props.get("context").getValue(), this.props.get("limit").getValue(), this.props.get("sort").getValue(), this.props.get("order").getValue(), this.props.get("page").getValue());
            this.setResult(r);
        } catch (Exception e){
            this.setResult(new Result("Kwic failed " + e.getMessage()));
        }
    }
}
