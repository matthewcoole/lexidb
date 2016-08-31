package uk.ac.lancs.ucrel.conv;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class Words {

    private Map<String, Integer> words = new HashMap<String, Integer>();
    private List<Word> list = new ArrayList<Word>();
    private int chars = 0;
    private int wordCount = 0;

    private class Word implements Comparable<Word> {

        protected String word;
        protected int count;

        public Word(String s, int i){
            this.word = s;
            this.count = i;
        }

        @Override
        public int compareTo(Word w) {
            return w.count - this.count;
        }

        @Override
        public String toString(){
            return word/* + "[" + count + "]"*/;
        }
    }

    public Words(Path corpus) throws IOException {
        Files.walkFileTree(corpus ,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                try {
                    List<String> lines = Files.readAllLines(file);
                    for(String line : lines){
                        StringTokenizer st = new StringTokenizer(line);
                        while(st.hasMoreTokens()){
                            String word = st.nextToken().replaceAll("[^A-Za-z ]", "").toLowerCase();
                            chars += word.length();
                            wordCount++;
                            if(!words.containsKey(word))
                                words.put(word, 0);
                            words.put(word, words.get(word) + 1);
                        }
                    }
                } catch (Exception e){
                    System.err.println(e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }
        });

        for(String word : words.keySet()){
            list.add(new Word(word, words.get(word)));
        }

        Collections.sort(list);
    }

    public static void main(String[] args) throws IOException {
        Words w = new Words(Paths.get("/home/mpc/Desktop/lob/src"));

        System.out.println("mean word length: " + w.mean());
        /*System.out.println("top: " + w.topWords(10));
        System.out.println("91+: " + w.getWords(99, 91, 10));
        System.out.println("81+: " + w.getWords(90, 81, 10));
        System.out.println("71+: " + w.getWords(80, 71, 10));
        System.out.println("61+: " + w.getWords(70, 61, 10));
        System.out.println("51+: " + w.getWords(60, 51, 10));
        System.out.println("1+: " + w.getWords(50, 1, 50));*/
        List<String> words = new ArrayList<String>();
        words.addAll(w.topWords(10));
        words.addAll(w.getWords(99, 91, 10));
        words.addAll(w.getWords(90, 81, 10));
        words.addAll(w.getWords(80, 71, 10));
        words.addAll(w.getWords(70, 61, 10));
        words.addAll(w.getWords(60, 51, 10));
        words.addAll(w.getWords(50, 1, 10));
        for(String s : words){
            System.out.println(s);
        }
    }

    public int mean(){
        return chars/wordCount;
    }

    public List<String> topWords(int count){
        List<String> words = new ArrayList<>();
        for(int i = 0; i < count; i++){
            words.add(list.get(i).toString());
        }
        return words;
    }

    public List<String> getWords(int upperPercentile, int lowerPercentile, int count){
        List<String> words = new ArrayList<String>();

        int percentile = this.list.size() / 100;
        int start = list.size() - (upperPercentile * percentile);
        int end = list.size() - (lowerPercentile * percentile);
        int randLimit = end - start;
        Random r = new Random();
        for(int i = 0; i < count; i++){
            int n = start + r.nextInt(randLimit);
            words.add(list.get(n).toString());
        }
        return words;
    }
}
