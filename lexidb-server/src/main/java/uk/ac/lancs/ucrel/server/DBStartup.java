package uk.ac.lancs.ucrel.server;

import uk.ac.lancs.ucrel.node.Node;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

public class DBStartup {

    public static void main(String[] args) throws RemoteException, AlreadyBoundException, InterruptedException {
        Properties p = loadNodeProperties();
        Node n = new Node(p);
        n.start();
    }

    private static Properties loadNodeProperties() {
        Properties p = new Properties();
        InputStream is = DBStartup.class.getClassLoader().getResourceAsStream("node.properties");
        try {
            p.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }
}
