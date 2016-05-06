package uk.ac.lancs.ucrel.cli.commands;

import uk.ac.lancs.ucrel.rmi.result.Result;

public abstract class Command {

    private String name;
    private String desc;
    private String[] params;
    private Result r;

    public Command(String name){
        this.name = name;
    }

    public Command(String name, String desc, String... params){
        this.name = name;
        this.desc = desc;
        this.params = params;
    }

    public String getName(){
        return name;
    }

    public String getDesc(){
        return desc;
    }

    public Result getResult(){
        return r;
    }

    public void setResult(Result r){
        this.r = r;
    }

    public String[] getParams(){
        return params;
    }

    public void setParams(String[] params){
        this.params = params;
    }

    public void invoke(){
        System.err.println("Unimplemented command!");
    }
}
