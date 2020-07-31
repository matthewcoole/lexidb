package query;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import lombok.Data;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Data
public class TokenQuery {

    private Map<Character, Map<String, String>> qbeObjects = new HashMap<>();
    private RunAutomaton query;
    private String queryString;

    public TokenQuery(String query) throws IOException {
        char replacement = 'a';
        int start;
        while ((start = query.indexOf("{\"")) > -1) {
            int end = query.indexOf("\"}");
            String qbe = query.substring(start, end + 2);
            Map<String, String> qbeObject = new ObjectMapper().readValue(qbe, HashMap.class);
            qbeObjects.put(replacement, qbeObject);
            query = query.replace(qbe, String.valueOf(replacement));
            replacement += 1;
        }
        RegExp re = new RegExp(query);
        this.query = new RunAutomaton(re.toAutomaton());
        this.queryString = query;
    }
}
