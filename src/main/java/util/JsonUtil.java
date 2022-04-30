package util;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonUtil {

    private JsonUtil() {
        super();
    }

    public static JSONObject getJsonObj(String file) throws IOException {
        String jsonStr = new String(Files.readAllBytes(Path.of(file)));
        return new JSONObject(jsonStr);
    }
}
