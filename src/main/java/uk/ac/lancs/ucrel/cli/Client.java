package uk.ac.lancs.ucrel.cli;

import uk.ac.lancs.ucrel.cli.commands.*;
import uk.ac.lancs.ucrel.cli.commands.Set;
import uk.ac.lancs.ucrel.rmi.Server;
import uk.ac.lancs.ucrel.rmi.result.Result;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    private Registry r;
    private Server s;
    private boolean connected = false;
    private Result last;
    private Map<String, Param> params;
    private ArrayList<String> history;
    private Map<String, Command> commands;

    public static void main(String[] args) {
        Client c = new Client();
        while(c.isConnected()) {
            String cmd = c.getCommand();
            c.runCommand(cmd);
        }
    }

    private Client() {
        try {
            r = LocateRegistry.getRegistry(1289);
            Remote tmp = r.lookup("serv");
            if (tmp instanceof Server)
                s = (Server) tmp;
            params = Param.getDefaultParams();
            commands = new HashMap<String, Command>();
            commands.put("get", new Get(params));
            commands.put("help", new Help(commands));
            commands.put("shutdown", new Shutdown(s));
            commands.put("exit", new Exit());
            commands.put("set", new Set(params));
            commands.put("insert", new Insert(s));
            commands.put("kwic", new Kwic(s, params));
            commands.put("it", new It(s));
            history = new ArrayList<String>();
            connected = true;
        } catch(Exception e){
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    private boolean isConnected(){
        return connected;
    }

    private String getCommand(){
        pause();
        System.out.print("discoDB > ");
        try {
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            return buffer.readLine();
        } catch (Exception e){
            return "";
        }
    }

    private void pause(){
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void runCommand(String cmd){
        try {
            String op = getMethod(cmd);
            String[] params = getParams(cmd);
            history.add(cmd);
            if (commands.containsKey(op)){
                Command c = commands.get(op);
                c.setParams(params);
                c.invoke();
                c.getResult().print();
            }
        } catch (Exception e){
            System.err.println("Command failed!: " + e.getMessage());
        }
    }

    private String getMethod(String cmd){
        return new StringTokenizer(cmd).nextToken();
    }

    private String[] getParams(String cmd){
        StringTokenizer st = new StringTokenizer(cmd);
        List<String> params = new ArrayList<String>();
        st.nextToken();
        while(st.hasMoreTokens()){
            params.add(st.nextToken());
        }
        return params.toArray(new String[params.size()]);
    }
}
