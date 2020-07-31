package util;

import storage.Corpus;

import java.io.IOException;

public class Merge {
    public static void main(String[] args) throws IOException {
        Corpus c = new Corpus(args[0], args[1]);
        c.reloadConf();
        c.mergeBlocks(Integer.parseInt(args[2]), Integer.parseInt(args[3]));
    }
}
