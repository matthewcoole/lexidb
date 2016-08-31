package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ngram.NGram;
import uk.ac.lancs.ucrel.sort.ngram.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalNgramImpl implements Ngram {

    private Path dataPath;
    private int pageLength, currentPos;
    private List<String> words;
    private long time;
    private
    List<int[]> contexts;
    Map<String, NGram> ngramsMap = new HashMap<String, NGram>();
    List<NGram> ngrams = new ArrayList<NGram>();

    public LocalNgramImpl(Path dataPath){
        this.dataPath = dataPath;
    }

    @Override
    public void search(String searchTerm, int n, int pos, int pageLength) throws RemoteException {
        try {
            System.out.println("kwic for " + searchTerm);
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            words = ca.getWords(searchTerm);
            if(pos == 0){
                contexts = new ArrayList<int[]>();
                for(int i = 0; i < n; i++){
                    contexts.addAll(ca.context(words, i, n-(i+1), 0));
                }
            } else {
                contexts = ca.context(words, pos-1, n - (pos - 1), 0);
            }
            for(int[] context : contexts){
                String k = getKey(context);
                if(!ngramsMap.containsKey(k)){
                    NGram ng = new NGram();
                    for(int i : context){
                        ng.add(ca.getWord(i));
                    }
                    ngramsMap.put(k, ng);
                }
                NGram ng = ngramsMap.get(k);
                int count = ng.getCount() + 1;
                ng.setCount(count);
            }
            ngrams.addAll(ngramsMap.values());
            Collections.sort(ngrams, new FrequencyComparator());
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    private String getKey(int[] context){
        StringBuilder sb = new StringBuilder();
        for(int i : context){
            sb.append(i).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    @Override
    public List<NGram> it() throws RemoteException {
        List<NGram> page = new ArrayList<NGram>();
        for(int i = currentPos + pageLength; currentPos < i; currentPos++){
            page.add(ngrams.get(currentPos));
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return 0;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
