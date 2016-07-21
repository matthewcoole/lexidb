package uk.ac.lancs.ucrel.result;

import uk.ac.lancs.ucrel.conc.ConcordanceLine;
import uk.ac.lancs.ucrel.corpus.CorpusAccessor;

import java.util.List;

public interface FullResult {
    List<ConcordanceLine> it(CorpusAccessor ca);
}
