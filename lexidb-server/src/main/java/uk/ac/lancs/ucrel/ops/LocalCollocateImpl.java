package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.Word;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ngram.NGram;
import uk.ac.lancs.ucrel.sort.col.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalCollocateImpl implements Collocate{

    private Path dataPath;
    private long time;
    private int pageLength, currentPos;
    private Map<String, uk.ac.lancs.ucrel.col.Collocate> collocatesMap;
    private List<uk.ac.lancs.ucrel.col.Collocate> collocates;


    public LocalCollocateImpl(Path dataPath){
        this.dataPath = dataPath;

    }

    @Override
    public void search(String searchTerm, int leftContext, int rightContext, int pageLength) throws RemoteException {
        try {
            System.out.println("Collocation search for " + searchTerm);
            this.pageLength = pageLength;
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<String> words = ca.getWords(searchTerm);
            List<int[]> contexts = ca.context(words, leftContext, rightContext, 0);
            collocatesMap = new HashMap<String, uk.ac.lancs.ucrel.col.Collocate>();
            for(int[] c : contexts){
                for(int i : c){
                    Word w = ca.getWord(i);
                    if(!collocatesMap.containsKey(w.toString()))
                        collocatesMap.put(w.toString(), new uk.ac.lancs.ucrel.col.Collocate(w, searchTerm, 0));
                    collocatesMap.get(w.toString()).increment();
                }
            }
            for(String s : words){
                collocatesMap.remove(s);
            }
            collocates = new ArrayList<uk.ac.lancs.ucrel.col.Collocate>();
            collocates.addAll(collocatesMap.values());
            Collections.sort(collocates, new FrequencyComparator());
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e){
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public List<uk.ac.lancs.ucrel.col.Collocate> it() throws RemoteException {
        List<uk.ac.lancs.ucrel.col.Collocate> page = new ArrayList<uk.ac.lancs.ucrel.col.Collocate>();
        for(int i = currentPos + pageLength; currentPos < i; currentPos++){
            page.add(collocates.get(currentPos));
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
