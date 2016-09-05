package uk.ac.lancs.ucrel.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Word implements Serializable {

    private String originalForm;
    private List<String> tags = new ArrayList<String>();

    public Word(String originalForm) {
        this.originalForm = originalForm.trim();
    }

    public void addTag(String tag) {
        tags.add(tag.trim());
    }

    public List<String> getTags() {
        return tags;
    }

    public String toString() {
        return originalForm;
    }
}
