package properties;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class AppProperties {

    private static final Map<String, String> DEFAULTS = new HashMap<>();
    private static Properties prop = new Properties();

    static {
        DEFAULTS.put("block.cache.size", "100");
        DEFAULTS.put("block.cache.timeout", "1000");
        DEFAULTS.put("corpus.cache.size", "10");
        DEFAULTS.put("corpus.cache.timeout", "1000");
        DEFAULTS.put("result.cache.size", "100");
        DEFAULTS.put("result.cache.timeout", "30");
        DEFAULTS.put("data.path", "lexi-data");
        DEFAULTS.put("kwic.context", "5");
        DEFAULTS.put("result.page.size", "100");
        DEFAULTS.put("block.size", "10000000");
    }

    public static void loadProps(String path) {
        try (InputStream is = Files.newInputStream(Paths.get(path))) {
            prop.load(is);
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("Failed to load " + path);
        }
    }

    public static String get(String key) {
        if (prop.containsKey(key))
            return prop.getProperty(key);
        if (DEFAULTS.containsKey(key))
            return DEFAULTS.get(key);
        log.warn("Property " + key + " not found!");
        return "";
    }

    public static void set(String key, String val) {
        prop.put(key, val);
    }
}
