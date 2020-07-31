package result;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class KwicResultPage extends Result {
    public List<List<Map<String, String>>> concordances = new ArrayList<>();

    public String toString(String column) {
        StringBuilder sb = new StringBuilder();
        for (List<Map<String, String>> conc : concordances) {
            for (Map<String, String> token : conc) {
                sb.append(token.get(column));
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }
}
