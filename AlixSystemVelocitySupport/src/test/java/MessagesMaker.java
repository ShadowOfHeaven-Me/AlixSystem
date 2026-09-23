import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class MessagesMaker {

    public static void main(String[] args) throws IOException {
        String separator = ":";
        try (var in = MessagesMaker.class.getClassLoader().getResourceAsStream("alix/loaders/velocity/messages.properties")) {
            var reader = new BufferedReader(new InputStreamReader(in));

            String line;
            Map<String, String> map = new TreeMap<>();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.trim().startsWith("#")) continue;//comment/header line, not a message key
                var a = line.split(separator, 2);
                map.put(a[0], a[1].trim());
            }
            map.forEach((k, v) -> System.out.println(k + separator + " " + v));
        }
    }
}