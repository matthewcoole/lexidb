package util;

import lombok.Generated;
import properties.AppProperties;
import storage.Corpus;

import java.io.IOException;

@Generated
public class Insert {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        AppProperties.loadProps(args[0]);
        new Insert().insert(args[1], args[2], args[3]);
    }

    public void insert(String corpusName, String confPath, String dir) throws IOException {
        Corpus c = new Corpus(corpusName, AppProperties.get("data.path"));
        c.loadConf(confPath);
        c.addFiles(dir, "tsv");
        c.save();
    }
}
