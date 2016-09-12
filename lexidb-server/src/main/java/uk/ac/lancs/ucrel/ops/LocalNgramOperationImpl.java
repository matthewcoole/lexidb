package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Ngram;
import uk.ac.lancs.ucrel.sort.ngram.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalNgramOperationImpl implements NgramOperation {

    Map<String, Ngram> ngramsMap = new HashMap<String, Ngram>();
    List<Ngram> ngrams = new ArrayList<Ngram>();
    private Path dataPath;
    private int pageLength, currentPos;
    private List<DictionaryEntry> words;
    private long time;

    public LocalNgramOperationImpl(Path dataPath) {
        this.dataPath = dataPath;
    }

    @Override
    public void search(String[] searchTerms, int n, int pos, int pageLength, boolean reverseOrder) throws RemoteException {
        try {
            System.out.println("ngram for " + searchTerms);
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            words = ca.getWords(Arrays.asList(searchTerms));
            List<int[]> contexts;

            /*TODO: This can be improved by working out how big the context needs to be to capture all ngrams in one
            search and then simply shifting through each context to extract each ngram*/
            if (pos == 0) {
                contexts = new ArrayList<int[]>();
                for (int i = 0; i < n; i++) {
                    contexts.addAll(ca.context(words, i, n - (i + 1), 0));
                }
            } else {
                contexts = ca.context(words, pos - 1, n - (pos - 1), 0);
            }
            for (int[] context : contexts) {
                String k = getKey(context);
                if (!ngramsMap.containsKey(k)) {
                    Ngram ng = new Ngram();
                    for (int i : context) {
                        ng.add(ca.getWord(i));
                    }
                    ngramsMap.put(k, ng);
                }
                Ngram ng = ngramsMap.get(k);
                int count = ng.getCount() + 1;
                ng.setCount(count);
            }
            ngrams.addAll(ngramsMap.values());
            Collections.sort(ngrams, new FrequencyComparator());
            if (reverseOrder)
                Collections.reverse(ngrams);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
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
            if(currentPos == ngrams.size() - 1)
                break;
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return ngrams.size();
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
