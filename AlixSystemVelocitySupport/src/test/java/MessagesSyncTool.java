import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Dev-only tool (never shipped - lives in src/test/java, same as MessagesMaker) that keeps every
 * langs/*.properties file's KEY SET in sync with the canonical messages.properties file, without ever
 * touching an already-translated value. Run it (plain "java MessagesSyncTool.java", no dependencies) from
 * the AlixSystemVelocitySupport module root after adding/removing a key in messages.properties:
 * <p>
 * - any key present in messages.properties but missing from a langs/*.properties file is appended there,
 * with the English text as a placeholder, prefixed by a "#TRANSLATE:" marker comment on the line above -
 * grep for that marker to find everything still needing a human translation.
 * - any key present in a langs/*.properties file but no longer in messages.properties is only reported,
 * never auto-removed - a human should confirm a translation is actually dead before deleting it.
 * <p>
 * This does NOT translate anything by itself - an actual translation still needs a human (or a separate,
 * deliberate pass) to replace the placeholder text; it only makes sure a key is never silently missing from
 * a language file the way messages.properties/langs/cs.properties could previously drift apart.
 */
public final class MessagesSyncTool {

    private static final String SEPARATOR = ":";
    private static final String TRANSLATE_MARKER = "#TRANSLATE: ";

    //Every non-English bundled translation this plugin ships - keep in sync with VelocityAlixMain.ParamImpl's
    //SUPPORTED_LANGUAGES (minus "en", which messages.properties itself always represents)
    private static final List<String> LANGUAGES = List.of("cs");

    public static void main(String[] args) throws IOException {
        Path resourceRoot = findResourceRoot();
        Path reference = resourceRoot.resolve("messages.properties");

        LinkedHashMap<String, String> referenceEntries = parse(reference);
        System.out.println("Reference (messages.properties): " + referenceEntries.size() + " keys");

        for (String lang : LANGUAGES) {
            Path langFile = resourceRoot.resolve("langs/" + lang + ".properties");
            LinkedHashMap<String, String> langEntries = parse(langFile);

            List<String> missing = referenceEntries.keySet().stream()
                    .filter(k -> !langEntries.containsKey(k))
                    .collect(Collectors.toList());
            List<String> stale = langEntries.keySet().stream()
                    .filter(k -> !referenceEntries.containsKey(k))
                    .collect(Collectors.toList());

            if (missing.isEmpty() && stale.isEmpty()) {
                System.out.println(lang + ".properties: already in sync (" + langEntries.size() + " keys)");
                continue;
            }

            if (!missing.isEmpty()) {
                StringBuilder append = new StringBuilder();
                String existing = Files.readString(langFile, StandardCharsets.UTF_8);
                if (!existing.isEmpty() && !existing.endsWith("\n")) append.append('\n');
                for (String key : missing) {
                    append.append(TRANSLATE_MARKER).append(key).append('\n');
                    append.append(key).append(SEPARATOR).append(' ').append(quote(referenceEntries.get(key))).append('\n');
                }
                Files.writeString(langFile, append.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
                System.out.println(lang + ".properties: appended " + missing.size() + " missing key(s) (English placeholder, marked with '" + TRANSLATE_MARKER.trim() + "') - " + missing);
            }

            if (!stale.isEmpty())
                System.out.println(lang + ".properties: " + stale.size() + " key(s) no longer in messages.properties (NOT removed automatically - confirm before deleting) - " + stale);
        }
    }

    private static String quote(String value) {
        return value.startsWith("\"") && value.endsWith("\"") ? value : "\"" + value + "\"";
    }

    private static LinkedHashMap<String, String> parse(Path file) throws IOException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (!Files.exists(file)) return map;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            int sep = line.indexOf(SEPARATOR);
            if (sep < 0) continue;
            String key = line.substring(0, sep).trim();
            String value = line.substring(sep + 1).trim();
            map.put(key, value);
        }
        return map;
    }

    private static Path findResourceRoot() {
        Path candidate = Path.of("src/main/resources/alix/loaders/velocity");
        if (Files.isDirectory(candidate)) return candidate;
        candidate = Path.of("AlixSystemVelocitySupport/src/main/resources/alix/loaders/velocity");
        if (Files.isDirectory(candidate)) return candidate;
        throw new IllegalStateException("Could not locate the 'alix/loaders/velocity' resources folder from working directory " + Path.of("").toAbsolutePath());
    }
}
