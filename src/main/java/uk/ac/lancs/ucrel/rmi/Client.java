package uk.ac.lancs.ucrel.rmi;

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
    private Set<String> methods;

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
            methods = new HashSet<String>();
            for(Method m : this.getClass().getMethods()){
                methods.add(m.getName());
            }
            connected = true;
        } catch(Exception e){
            System.err.println("Could not connect to server: " + e.getMessage());
        }
    }

    public void exit(){
        System.exit(0);
    }

    public void search(String s){
        System.out.println("Searching for " + s);
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
            if (op.equals("exit"))
                System.exit(0);
            else if (op.equals("shutdown")) {
                s.shutdown();
                System.exit(0);
            }
            else if(op.equals("insert")) {
                Result r = s.insert(params[0]);
                System.out.println(r);
            }
            else if(op.equals("kwic")) {
                Result r;
                if(params.length == 2){
                    int sort = Integer.parseInt(params[1]);
                    r = s.search(params[0], sort);
                } else {
                    r = s.search(params[0]);
                }
                System.out.println(r);
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

    private Class[] getParamClasses(String[] params){
        List<Class> classes = new ArrayList<Class>();
        for(String s : params){
            classes.add(String.class);
        }
        return classes.toArray(new Class[classes.size()]);
    }
}
