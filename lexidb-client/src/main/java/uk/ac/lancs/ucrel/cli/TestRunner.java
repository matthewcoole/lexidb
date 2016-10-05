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
import java.util.*;

public class TestRunner {
    public static void main(String[] args) throws RemoteException {
        String host = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 1289;
        Client c = new Client(host, port);
        Server s = c.getServer();

        int nterms = 10;
        Map<String, Integer> words = getWordList(s, nterms);
        int runs = 3;

        System.out.println("Word Frequency");
        for(String wle : words.keySet()){
            System.out.println(wle + ", " + words.get(wle));
        }
        System.out.println();

        kwic(s, runs, words.keySet());
        col(s, runs, words.keySet());
        ngram(s, runs, words.keySet());
        list(s, runs, nterms);
    }

    private static void ngram(Server s, int runs, Collection<String> terms) throws RemoteException {
        System.out.println("ngram (" + runs + " runs)");
        for(String word : terms){
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

    private static void col(Server s, int runs, Collection<String> terms) throws RemoteException {
        System.out.println("cols (" + runs + " runs)");
        for(String word : terms){
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

    private static void kwic(Server s, int runs, Collection<String> terms) throws RemoteException {
        System.out.println("kwics (" + runs +" runs)");
        for(String word : terms){
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

    private static int count(Server s, String term) throws RemoteException{
        ListOperation l = s.list();
        l.search(new String[]{term}, 20, false);
        List<WordListEntry> wl = l.it();
        int count = 0;
        for(WordListEntry wle : wl){
            count += wle.getCount();
        }
        return count;
    }

    private static Map<String, Integer> getWordList(Server s, int n) throws RemoteException {
        List<WordListEntry> words = listResults(s, n*3);
        Map<String, Integer> wordList = new HashMap<String, Integer>();
        for(WordListEntry wle : words){
            if(wle.getWord().toString().matches("[a-zA-Z]+") && !wordList.containsKey(wle.getWord().toString()))
                wordList.put(wle.getWord().toString(), count(s, wle.getWord().toString()));
            if(wordList.size() >= n)
                break;
        }
        return wordList;
    }
}
