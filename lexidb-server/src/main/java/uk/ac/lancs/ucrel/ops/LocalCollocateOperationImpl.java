package uk.ac.lancs.ucrel.ops;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.ds.Collocate;
import uk.ac.lancs.ucrel.ds.Word;
import uk.ac.lancs.ucrel.sort.col.FrequencyComparator;

import java.nio.file.Path;
import java.rmi.RemoteException;
import java.util.*;

public class LocalCollocateOperationImpl implements CollocateOperation {

    private Path dataPath;
    private long time;
    private int pageLength, currentPos;
    private Map<String, Collocate> collocatesMap;
    private List<Collocate> collocates;


    public LocalCollocateOperationImpl(Path dataPath) {
        this.dataPath = dataPath;

    }

    @Override
    public void search(String searchTerm, int leftContext, int rightContext, int pageLength, boolean reverseOrder) throws RemoteException {
        try {
            System.out.println("Collocation search for " + searchTerm);
            this.pageLength = pageLength;
            long start = System.currentTimeMillis();
            CorpusAccessor ca = CorpusAccessor.getAccessor(dataPath);
            List<String> words = ca.getWords(searchTerm);
            List<int[]> contexts = ca.context(words, leftContext, rightContext, 0);
            collocatesMap = new HashMap<String, Collocate>();
            for (int[] c : contexts) {
                for (int i : c) {
                    Word w = ca.getWord(i);
                    if (!collocatesMap.containsKey(w.toString()))
                        collocatesMap.put(w.toString(), new Collocate(w, 0));
                    collocatesMap.get(w.toString()).increment();
                }
            }
            for (String s : words) {
                collocatesMap.remove(s);
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
            if(currentPos == collocates.size() - 1)
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
