package storage;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class JSONUtil {

    public static void savetoJSON(Path p, DataBlock css) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter().writeValue(p.toFile(), css);
    }

    public static DataBlock loadColumnSuperSet(Path p) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(p.toFile(), DataBlock.class);
    }
}
