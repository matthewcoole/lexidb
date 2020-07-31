package result;

import lombok.Data;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListResult extends Result {
    public List<Pair<String, Integer>> list = new ArrayList<>();

    public ListResultPage getPage(int page, int pageSize) {
        ListResultPage lrp = new ListResultPage();
        lrp.setResultCount(list.size());
        lrp.setResultsPerPage(pageSize);
        lrp.setPages(lrp.getResultCount() % lrp.getResultsPerPage() == 0 ? lrp.getResultCount() / lrp.getResultsPerPage() : lrp.getResultCount() / lrp.getResultsPerPage() + 1);
        lrp.setPage(page + 1);
        int start = pageSize * page;
        int end = start + pageSize;
        int min = list.size() < start ? list.size() : start;
        int max = list.size() < end ? list.size() : end;
        lrp.list = list.subList(min, max);
        return lrp;
    }

    /*public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Pair p : list) {
            sb.append(p.getKey()).append(':').append(p.getValue()).append('\n');
        }
        return sb.toString().trim();
    }*/
}
