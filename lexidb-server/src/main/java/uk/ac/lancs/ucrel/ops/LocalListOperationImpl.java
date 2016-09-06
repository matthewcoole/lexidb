package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.ds.WordListEntry;
import uk.ac.lancs.ucrel.sort.list.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalListOperationImpl implements ListOperation {

    private Path dataPath;
    private long time;
    private int pageLength, currentPos;
    private List<WordListEntry> wordlist;


    public LocalListOperationImpl(Path dataPath) {
        this.dataPath = dataPath;

    }

    @Override
    public void search(String searchTerm, int pageLength, boolean reverseOrder) throws RemoteException {
        try {
            System.out.println("Collocation search for " + searchTerm);
            this.pageLength = pageLength;
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<String> words = ca.getWords(searchTerm);

            //TODO
            Map<Integer, Integer> wordlistVals = ca.list(words);
            wordlist = new ArrayList<WordListEntry>();
            for(int i : wordlistVals.keySet()){
                Word w = ca.getWord(i);
                int count = wordlistVals.get(i);
                wordlist.add(new WordListEntry(w, count));
            }

            Collections.sort(wordlist, new FrequencyComparator());
            if (reverseOrder)
                Collections.reverse(wordlist);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public List<WordListEntry> it() throws RemoteException {
        List<WordListEntry> page = new ArrayList<WordListEntry>();
        for (int i = currentPos + pageLength; currentPos < i; currentPos++) {
            page.add(wordlist.get(currentPos));
            if(currentPos == wordlist.size() - 1)
                break;
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return wordlist.size();
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
