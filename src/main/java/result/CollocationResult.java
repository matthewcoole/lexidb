package result;

import lombok.Data;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class CollocationResult extends Result {
    private List<Pair<String, Map<String, Double>>> collocations = new ArrayList<>();

    public CollocationResultPage getPage(int page, int pageSize){
        CollocationResultPage crp = new CollocationResultPage();
        crp.setResultCount(collocations.size());
        crp.setResultsPerPage(pageSize);
        crp.setPages(crp.getResultCount() % crp.getResultsPerPage() == 0 ? crp.getResultCount() / crp.getResultsPerPage() : crp.getResultCount() / crp.getResultsPerPage() + 1);
        crp.setPage(page + 1);
        crp.setBlockQueried(this.getBlockQueried());
        crp.setTotalBlocks(this.getTotalBlocks());
        int start = pageSize * page;
        int end = start + pageSize;
        int min = collocations.size() < start ? collocations.size() : start;
        int max = collocations.size() < end ? collocations.size() : end;
        crp.setCollocations(collocations.subList(min, max));
        return crp;
    }
}

