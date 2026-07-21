import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class SpigotMessagesMaker {

    public static void main(String[] args) throws IOException {
        String separator = ":";
        try (var in = SpigotMessagesMaker.class.getClassLoader().getResourceAsStream("messages.txt")) {
            var reader = new BufferedReader(new InputStreamReader(in));

            String line;
            Map<String, String> map = new TreeMap<>();
            while ((line = reader.readLine()) != null) {
                var a = line.split(separator, 2);
                if (a.length == 1) a = line.split("=", 2);
                map.put(a[0], a[1].trim());
            }
            map.forEach((k, v) -> System.out.println(k + separator + " " + v));
        }
    }
}