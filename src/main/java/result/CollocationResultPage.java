package result;

import lombok.Data;
import util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class CollocationResultPage extends Result {
    private List<Pair<String, Map<String, Double>>> collocations = new ArrayList<>();
}
