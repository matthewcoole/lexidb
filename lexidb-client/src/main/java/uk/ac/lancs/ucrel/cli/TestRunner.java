package uk.ac.lancs.ucrel.cli;

import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.ds.Ngram;
import uk.ac.lancs.ucrel.ds.WordListEntry;
import uk.ac.lancs.ucrel.ops.CollocateOperation;
import uk.ac.lancs.ucrel.ops.KwicOperation;
import uk.ac.lancs.ucrel.ops.ListOperation;
import uk.ac.lancs.ucrel.ops.NgramOperation;
import uk.ac.lancs.ucrel.rmi.Server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class TestRunner {
    public static void main(String[] args) throws RemoteException {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 1289;
        Client c = new Client(host, port);
        Server s = c.getServer();

        int nterms = 10;
        List<WordListEntry> words = getWordList(s, nterms);
        int runs = 3;

        System.out.println("Word Frequency");
        for(WordListEntry wle : words){
            System.out.println(wle.getWord().toString() + ", " + wle.getCount());
        }
        System.out.println();

        kwic(s, runs, words);
        col(s, runs, words);
        ngram(s, runs, words);
        list(s, runs, nterms);
    }

    private static void ngram(Server s, int runs, List<WordListEntry> terms) throws RemoteException {
        System.out.println("ngram (" + runs + " runs)");
        for(WordListEntry wle : terms){
            String word = wle.getWord().toString();
            long time = ngram(s, runs, word, 2);
            System.out.println(word + ", " + time);
        }
        System.out.println();
    }

    private static long ngram(Server s, int runs, String term, int n) throws RemoteException {
        long start = System.currentTimeMillis();
        for(int i = 0; i < runs; i++){
            ngramResults(s, term, n);
        }
        long end = System.currentTimeMillis();
        return (end - start)/runs;
    }

    private static List<Ngram> ngramResults(Server s, String term, int n) throws RemoteException {
        NgramOperation ng = s.ngram();
        ng.search(new String[]{term}, n, 0, 20, false);
        return ng.it();
    }

    private static void col(Server s, int runs, List<WordListEntry> terms) throws RemoteException {
        System.out.println("cols (" + runs + " runs)");
        for(WordListEntry wle : terms){
            String word = wle.getWord().toString();
            long time = col(s, runs, word);
            System.out.println(word + ", " + time);
        }
        System.out.println();
    }

    private static long col(Server s, int runs, String term) throws RemoteException {
        long start = System.currentTimeMillis();
        for(int i = 0; i < runs; i++){
            colResults(s, term);
        }
        long end = System.currentTimeMillis();
        return (end - start)/runs;
    }

    private static List<Collocate> colResults(Server s, String term) throws RemoteException {
        CollocateOperation c = s.collocate();
        c.search(new String[]{term}, 2, 2, 20, false);
        return c.it();
    }

    private static void kwic(Server s, int runs, List<WordListEntry> terms) throws RemoteException {
        System.out.println("kwics (" + runs +" runs)");
        for(WordListEntry wle : terms){
            String word = wle.getWord().toString();
            long time = kwic(s, runs, word);
            System.out.println(word + ", " + time);
        }
        System.out.println();
    }

    private static long kwic(Server s, int runs, String term) throws RemoteException {
        long start = System.currentTimeMillis();
        for(int i = 0; i < runs; i++){
            kwicResults(s, term);
        }
        long end = System.currentTimeMillis();
        return (end - start)/runs;
    }

    private static List<Kwic> kwicResults(Server s, String term) throws RemoteException {
        KwicOperation k = s.kwic();
        k.search(new String[]{term}, 5, 0, 0, 0, false, 20);
        return k.it();
    }

    private static void list(Server s, int runs, int n) throws RemoteException {
        System.out.println("list (" + runs + " runs)");
        long start = System.currentTimeMillis();
        for(int i = 0; i < runs; i++) {
            listResults(s, n);
        }
        long end = System.currentTimeMillis();
        System.out.println((end - start)/runs);
        System.out.println();
    }

    private static List<WordListEntry> listResults(Server s, int n) throws RemoteException {
        ListOperation l = s.list();
        l.search(new String[]{".*"}, n, false);
        return l.it();
    }

    private static List<WordListEntry> getWordList(Server s, int n) throws RemoteException {
        List<WordListEntry> words = listResults(s, n*3);
        List<WordListEntry> wordList = new ArrayList<WordListEntry>();
        for(WordListEntry wle : words){
            if(wle.getWord().toString().matches("[a-zA-Z]+") && !wordList.contains(wle.getWord().toString()))
                wordList.add(wle);
        }
        return wordList.subList(0, 20);
    }
}
