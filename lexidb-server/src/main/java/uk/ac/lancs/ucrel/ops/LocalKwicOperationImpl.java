package uk.ac.lancs.ucrel.ops;

import org.apache.log4j.Logger;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.sort.kwic.FrequencyComparator;
import uk.ac.lancs.ucrel.sort.kwic.LexicalComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LocalKwicOperationImpl implements KwicOperation {

    private static Logger LOG = Logger.getLogger(LocalKwicOperationImpl.class);

    private Path dataPath;
    private List<int[]> contexts;
    private List<DictionaryEntry> words;
    private long time;
    private int position, pageLength;

    public LocalKwicOperationImpl(Path dataPath) {
        this.dataPath = dataPath;
    }

    public void search(String[] searchTerms, int context, int limit, int sortType, int sortPos, boolean reverseOrder, int pageLength) throws RemoteException {
        try {
            LOG.debug("Kwic search for " + Arrays.toString(searchTerms));
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            words = ca.getWords(Arrays.asList(searchTerms));
            contexts = ca.context(words, context, context, limit);
            sort(sortType, sortPos, reverseOrder, context);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    @Override
    public List<Kwic> it() throws RemoteException {
        try {
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<Kwic> lines = new ArrayList<Kwic>();
            for (int i = position; i < contexts.size() && i < (position + pageLength); i++) {
                lines.add(ca.getLine(contexts.get(i)));
            }
            position += pageLength;
            return lines;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }

    public long getTime() {
        return time;
    }

    @Override
    public int getLength() throws RemoteException {
        return contexts.size();
    }

    public void sort(int type, int pos, boolean reverseOrder, int context) {
        if (type == 0)
            return;
        else if (type == 1) {
            Collections.sort(contexts, new LexicalComparator(context, pos));
        } else if (type == 2) {
            Collections.sort(contexts, new FrequencyComparator(context, pos, contexts));
        }
        if (reverseOrder)
            Collections.reverse(contexts);
    }
}
