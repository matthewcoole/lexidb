package uk.ac.lancs.ucrel.cli;

import java.util.HashMap;
import java.util.Map;

public class Param {

    public static final Object[][] DEFAULT_PARAMS = {
            {"page", 20, "Number of results to display per page"},
            {"sort", 0, "Postion of sort relative to keyword i.e. '-1' sorts on one word before."},
            {"context", 5, "How many words either side of the keyword to display"},
            {"limit", 0, "Maximum number of results to display. Value '0' returns all results."},
            {"order", 1, "Order to sort results. Negative value inverts order."}
    };

    public static Map<String, Param> getDefaultParams(){
        Map<String, Param> params = new HashMap<String, Param>();
        for(Object[] defaults : DEFAULT_PARAMS){
            params.put((String)defaults[0], new Param((String)defaults[0], (Integer)defaults[1], (String)defaults[2]));
        }
        return params;
    }

    private String name;
    private int value;
    private String desc;

    public Param(String name, int value, String desc){
        this.name = name;
        this.value = value;
        this.desc = desc;
    }

    public void setValue(int value){
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public String getDesc(){
        return desc;
    }

    public String getName(){
        return name;
    }
}
