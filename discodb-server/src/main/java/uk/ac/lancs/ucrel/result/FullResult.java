package uk.ac.lancs.ucrel.result;

import uk.ac.lancs.ucrel.corpus.CorpusAccessor;
import uk.ac.lancs.ucrel.rmi.result.Result;

public interface FullResult {
    Result it(CorpusAccessor ca);
}
