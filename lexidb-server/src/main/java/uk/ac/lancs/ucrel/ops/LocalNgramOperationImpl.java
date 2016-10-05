package uk.ac.lancs.ucrel.ops;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Ngram;
import uk.ac.lancs.ucrel.sort.ngram.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class LocalNgramOperationImpl implements NgramOperation {

    private Logger LOG = Logger.getLogger(LocalNgramOperationImpl.class);

    private ExecutorService es;
    private boolean complete = false;
    private Map<String, Ngram> ngramsMap = new HashMap<String, Ngram>();
    private List<Ngram> ngrams = new ArrayList<Ngram>();
    private Path dataPath;
    private int pageLength, currentPos;
    private List<DictionaryEntry> words;
    private long time;

    public LocalNgramOperationImpl(ExecutorService es, Path dataPath) {
        this.es = es;
        this.dataPath = dataPath;
    }

    @Override
    public void search(String[] searchTerms, int n, int pos, int pageLength, boolean reverseOrder) throws RemoteException {
        es.execute(()->searchRunner(searchTerms, n, pos, pageLength, reverseOrder));
    }

    public void searchRunner(String[] searchTerms, int n, int pos, int pageLength, boolean reverseOrder) {
        try {
            LOG.debug("ngram search for " + Arrays.toString(searchTerms));
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            words = ca.getWords(Arrays.asList(searchTerms));
            List<int[]> contexts;

            contexts = ca.context(words, getContextLeft(n, pos), getContextRight(n, pos), 0);


            for (int[] context : contexts) {
                int startPos = 0;
                while (startPos + n <= context.length) {
                    int[] ngram = new int[n];
                    for (int i = 0; i < n; i++) {
                        ngram[i] = context[startPos + i];
                    }
                    addNgramToMap(ngram, ca);
                    startPos++;
                }
            }
            ngrams.addAll(ngramsMap.values());
            Collections.sort(ngrams, new FrequencyComparator());
            if (reverseOrder)
                Collections.reverse(ngrams);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
        }
        complete = true;
    }

    private void addNgramToMap(int[] ngram, CorpusAccessor ca) {
        String k = getKey(ngram);
        if (!ngramsMap.containsKey(k)) {
            Ngram ng = new Ngram();
            for (int i : ngram) {
                ng.add(ca.getWord(i));
            }
            ngramsMap.put(k, ng);
        }
        Ngram ng = ngramsMap.get(k);
        int count = ng.getCount() + 1;
        ng.setCount(count);
    }

    private int getContextLeft(int n, int pos) {
        if (pos == 0)
            return n - 1;
        else
            return pos - 1;
    }

    private int getContextRight(int n, int pos) {
        if (pos == 0)
            return n - 1;
        else
            return n - pos;
    }

    private String getKey(int[] context) {
        StringBuilder sb = new StringBuilder();
        for (int i : context) {
            sb.append(i).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        return sb.toString();
    }

    @Override
    public List<Ngram> it() throws RemoteException {
        List<Ngram> page = new ArrayList<Ngram>();
        for (int i = currentPos + pageLength; currentPos < i; currentPos++) {
            page.add(ngrams.get(currentPos));
            if (currentPos == ngrams.size() - 1)
                break;
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return ngrams.size();
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
