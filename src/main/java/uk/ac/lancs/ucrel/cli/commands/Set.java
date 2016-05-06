package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.cli.Param;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.util.Map;

public class Set extends Command{

    private Map<String, Param> params;

    public Set(Map<String, Param> params){
        super("set", "Set a search parameter. Run \"get\" to view list of search parameters.", "param", "value");
        this.params = params;
    }

    public void invoke(){
        try {
            Param p = this.params.get(this.getParams()[0]);
            p.setValue(Integer.parseInt(this.getParams()[1]));
            this.setResult(new Result("Param set!"));
        } catch (Exception e){
            this.setResult(new Result("Could not update value: " + e.getMessage()));
        }
    }
}
