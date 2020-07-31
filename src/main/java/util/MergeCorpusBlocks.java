package util;

import properties.AppProperties;
import storage.Corpus;

import java.io.IOException;

public class MergeCorpusBlocks {
    public static void main(String[] args) throws IOException {
        AppProperties.loadProps(args[0]);
        new MergeCorpusBlocks().merge(args[1], Integer.parseInt(args[2]));
    }

    public void merge(String corpusName, int numberOfBlocks) throws IOException {
        Corpus c = new Corpus(corpusName, AppProperties.get("data.path"));
        c.cascadeMerge(numberOfBlocks);
    }
}
