package result;

import lombok.Data;
import query.json.SortProperty;
import storage.Corpus;
import util.Pair;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Data
public class KwicResult extends Result {

    private transient Corpus corpus;
    private List<int[]> lookup = new ArrayList<>();

    public KwicResultPage getPage(int page, int pageSize, int context) throws IOException, ExecutionException {
        KwicResultPage krp = new KwicResultPage();
        krp.setResultCount(lookup.size());
        krp.setResultsPerPage(pageSize);
        krp.setPages(krp.getResultCount() % krp.getResultsPerPage() == 0 ? krp.getResultCount() / krp.getResultsPerPage() : krp.getResultCount() / krp.getResultsPerPage() + 1);
        krp.setPage(page + 1);
        krp.setContext(context);
        krp.setBlockQueried(this.getBlockQueried());
        krp.setTotalBlocks(this.getTotalBlocks());
        krp.setSorted(isSorted());
        int start = pageSize * page;
        int end = start + pageSize;
        int min = lookup.size() < start ? lookup.size() : start;
        int max = lookup.size() < end ? lookup.size() : end;
        krp.setConcordances(corpus.retrieveConcordances(lookup.subList(min, max), context));
        return krp;
    }

    public void addAllToLookup(List<int[]> lookup) {
        this.lookup.addAll(lookup);
    }

    public KwicResultPage getAllResults() throws IOException, ExecutionException {
        KwicResultPage krp = new KwicResultPage();
        krp.setConcordances(corpus.retrieveConcordances(lookup.subList(0, lookup.size()), this.getContext()));
        return krp;
    }

    public KwicResultPage getAllResults(String... columns) throws IOException, ExecutionException {
        KwicResultPage krp = new KwicResultPage();
        krp.setConcordances(corpus.retrieveConcordances(lookup.subList(0, lookup.size()), this.getContext(), columns));
        return krp;
    }

    public void sort(SortProperty... sp) throws IOException, ExecutionException {

        if (sp.length == 0)
            return;

        List<List<String>> allVals = new ArrayList<>();

        for (SortProperty s : sp) {
            List<int[]> positionsToLookup = new ArrayList<>();
            for (int[] hit : lookup) {
                if (s.getPosition() == 0) {
                    positionsToLookup.add(new int[]{hit[0], hit[1], hit[1]});
                } else if (s.getPosition() < 0) {
                    positionsToLookup.add(new int[]{hit[0], hit[1] + s.getPosition(), hit[1] + s.getPosition()});
                } else {
                    positionsToLookup.add(new int[]{hit[0], hit[2] + s.getPosition(), hit[2] + s.getPosition()});
                }
            }
            allVals.add(corpus.lookup(positionsToLookup, s.getColumn()));
        }


        List<Pair<int[], List<String>>> sortable = new ArrayList<>();

        List<Map<String, Integer>> valueCounts = new ArrayList<>();
        for (SortProperty s : sp) {
            valueCounts.add(new HashMap<>());
        }

        for (int i = 0; i < lookup.size(); i++) {
            List<String> sortableProperties = new ArrayList<>();
            for (int j = 0; j < allVals.size(); j++) {
                String val = allVals.get(j).get(i);
                if (!valueCounts.get(j).containsKey(val)) {
                    valueCounts.get(j).put(val, 0);
                }
                int count = valueCounts.get(j).get(val) + 1;
                valueCounts.get(j).replace(val, count);
                sortableProperties.add(val);
            }
            Pair<int[], List<String>> sortablePair = new Pair<>(lookup.get(i), sortableProperties);
            sortable.add(sortablePair);
        }

        for (int i = 0; i < sp.length; i++) {
            if (!sp[i].isAlphabetical()) {
                for (int j = 0; j < sortable.size(); j++) {
                    String s = sortable.get(j).getValue().get(i);
                    sortable.get(j).getValue().set(i, Integer.toString(valueCounts.get(i).get(s)));
                }
            }
        }

        Collections.sort(sortable, new KwicComparator(sp));

        lookup = new ArrayList<>();

        for (Pair<int[], List<String>> sortedPair : sortable) {
            lookup.add(sortedPair.getKey());
        }
    }

    public void print(String column) {

    }

    public class KwicComparator implements Comparator<Pair<int[], List<String>>> {

        private SortProperty[] sp;

        public KwicComparator(SortProperty[] sp) {
            this.sp = sp;
        }

        public int compare(Pair<int[], List<String>> o1, Pair<int[], List<String>> o2) {
            for (int i = 0; i < o1.getValue().size(); i++) {
                String a = o1.getValue().get(i);
                String b = o2.getValue().get(i);
                int comparison = 0;
                if (sp[i].isAlphabetical()) {
                    comparison = a.compareTo(b);
                } else {
                    int x = Integer.parseInt(a);
                    int y = Integer.parseInt(b);
                    comparison = x - y;
                }
                if (comparison != 0) {
                    if (sp[i].isAscending())
                        return comparison;
                    else
                        return -comparison;
                }
            }
            return 0;
        }
    }
}
