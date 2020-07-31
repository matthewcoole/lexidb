package result;

import lombok.Data;
import util.Pair;

import java.util.ArrayList;
import java.util.List;

@Data
public class ListResultPage extends Result {
    public List<Pair<String, Integer>> list = new ArrayList<>();

    /*public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Pair p : list){
            sb.append(p.getKey()).append(':').append(p.getValue()).append('\n');
        }
        return sb.toString().trim();
    }*/
}
