package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.dict.DictionaryEntry;
import uk.ac.lancs.ucrel.ds.Kwic;
import uk.ac.lancs.ucrel.sort.kwic.FrequencyComparator;
import uk.ac.lancs.ucrel.sort.kwic.LexicalComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalKwicOperationImpl implements KwicOperation {

    private Path dataPath;
    private List<int[]> contexts;
    private List<String> words;
    private List<DictionaryEntry> newWords;
    private long time;
    private int position, pageLength;

    public LocalKwicOperationImpl(Path dataPath) {
        this.dataPath = dataPath;
    }
/*
    @Override
    public void search(String searchTerm, int context, int limit, int sortType, int sortPos, boolean reverseOrder, int pageLength) throws RemoteException {
        try {
            System.out.println("kwic for " + searchTerm);
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            words = ca.getWords(searchTerm);
            contexts = ca.context(words, context, context, limit);
            sort(sortType, sortPos, reverseOrder, context);
            long end = System.currentTimeMillis();
            time = end - start;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RemoteException(e.toString());
        }
    }
*/
    public void search(String[] searchTerm, int context, int limit, int sortType, int sortPos, boolean reverseOrder, int pageLength) throws RemoteException {
        try {
            long start = System.currentTimeMillis();
            this.pageLength = pageLength;
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            newWords = ca.getWords(Arrays.asList(searchTerm));
            contexts = ca.context(newWords, context, context, limit);
            sort(sortType, sortPos, reverseOrder, context);
            long end = System.currentTimeMillis();
            time = end -start;
        } catch (Exception e){
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
