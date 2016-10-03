package uk.ac.lancs.ucrel.ops;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.sort.col.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalCollocateOperationImpl implements CollocateOperation {

    private static Logger LOG = Logger.getLogger(LocalCollocateOperationImpl.class);

    private Path dataPath;
    private long time;
    private int pageLength, currentPos;
    private Map<String, Collocate> collocatesMap;
    private List<Collocate> collocates;


    public LocalCollocateOperationImpl(Path dataPath) {
        this.dataPath = dataPath;

    }

    @Override
    public void search(String[] searchTerms, int leftContext, int rightContext, int pageLength, boolean reverseOrder) throws RemoteException {
        try {
            LOG.debug("Collocation search for " + Arrays.toString(searchTerms));
            this.pageLength = pageLength;
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<DictionaryEntry> words = ca.getWords(Arrays.asList(searchTerms));
            List<int[]> contexts = ca.context(words, leftContext, rightContext, 0);
            collocatesMap = new HashMap<String, Collocate>();
            for (int[] c : contexts) {
                for (int i = 0; i < c.length; i++) {
                    Word w = ca.getWord(c[i]);
                    if (!collocatesMap.containsKey(w.toString()))
                        collocatesMap.put(w.toString(), new Collocate(w, leftContext + rightContext + 1, leftContext));
                    collocatesMap.get(w.toString()).increment(i);

                }
            }
            for (DictionaryEntry de : words) {
                collocatesMap.remove(de.getWord());
            }
            collocates = new ArrayList<Collocate>();
            collocates.addAll(collocatesMap.values());
            Collections.sort(collocates, new FrequencyComparator());
            if (reverseOrder)
                Collections.reverse(collocates);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }
    }

    @Override
    public List<Collocate> it() throws RemoteException {
        List<Collocate> page = new ArrayList<Collocate>();
        for (int i = currentPos + pageLength; currentPos < i; currentPos++) {
            page.add(collocates.get(currentPos));
            if (currentPos == collocates.size() - 1)
                break;
        }
        return page;
    }

    @Override
    public int getLength() throws RemoteException {
        return collocates.size();
    }

    @Override
    public long getTime() throws RemoteException {
        return time;
    }
}
