package uk.ac.lancs.ucrel.ops;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.ds.WordListEntry;
import uk.ac.lancs.ucrel.sort.list.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class LocalListOperationImpl implements ListOperation {

    private static Logger LOG = Logger.getLogger(LocalListOperationImpl.class);

    private ExecutorService es;
    private boolean complete = false;
    private Path dataPath;
    private long time;
    private int pageLength, currentPos;
    private List<WordListEntry> wordlist;


    public LocalListOperationImpl(ExecutorService es, Path dataPath) {
        this.es = es;
        this.dataPath = dataPath;
    }

    @Override
    public void search(String[] searchTerms, int pageLength, boolean reverseOrder) throws RemoteException {
        es.execute(()->searchRunner(searchTerms, pageLength, reverseOrder));
    }

    public void searchRunner(String[] searchTerms, int pageLength, boolean reverseOrder) {
        try {
            LOG.debug("List search for " + Arrays.toString(searchTerms));
            this.pageLength = pageLength;
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<DictionaryEntry> words = ca.getWords(Arrays.asList(searchTerms));

            Map<Integer, Integer> wordlistVals = ca.list(words);
            wordlist = new ArrayList<WordListEntry>();
            for (int i : wordlistVals.keySet()) {
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
        }
        complete = true;
    }

    @Override
    public List<WordListEntry> it() throws RemoteException {
        List<WordListEntry> page = new ArrayList<WordListEntry>();
        for (int i = currentPos + pageLength; currentPos < i; currentPos++) {
            page.add(wordlist.get(currentPos));
            if (currentPos == wordlist.size() - 1)
                break;
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return wordlist.size();
    }

    @Override
    public boolean isComplete() throws RemoteException {
        return complete;
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
