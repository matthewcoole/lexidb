package uk.ac.lancs.ucrel;

import org.apache.log4j.Logger;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Logger.getLogger(Main.class).info("Check logger");
        System.out.println("This will be the main class");
    }
}