package result;

import lombok.Data;

@Data
public abstract class Result {
    private int resultCount, page, resultsPerPage, pages, context, blockQueried, totalBlocks;
    private boolean sorted;
}
