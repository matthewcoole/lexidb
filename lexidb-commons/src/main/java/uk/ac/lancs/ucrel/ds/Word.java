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

    public String toString(boolean details) {
        if (details)
            return details();
        else
            return originalForm;
    }

    public String details() {
        StringBuilder sb = new StringBuilder();
        if(tags.size() > 0) {
            sb.append(originalForm).append('{');
            for (String tag : tags) {
                sb.append(tag).append(' ');
            }
            sb.deleteCharAt(sb.lastIndexOf(" "));
            sb.append('}');
        }
        return sb.toString();
    }
}
