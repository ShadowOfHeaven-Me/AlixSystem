import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Dev-only tool (never shipped - lives in src/test/java), successor to the old per-platform
 * MessagesMaker/SpigotMessagesMaker/MessagesSyncTool now that messages.properties/langs/cs.properties are
 * ONE shared file in AlixSystemLoader (both Spigot and Velocity bundle it as-is - see
 * AlixFileManager#getBundledResourceStream()'s relative-then-classloader fallback for why that already
 * works with no code changes). Run it ("java MessagesSyncTool.java", no build-time dependency) from the
 * AlixSystemLoader module root after adding/changing a key in messages.properties:
 * <p>
 * - any key present in messages.properties but missing from langs/cs.properties is machine-translated
 * (DeepL free API - see DEEPL_API_KEY below) and appended there, prefixed by a "#TRANSLATE-DRAFT:" marker
 * comment on the line above - grep for that marker to find everything still needing a human read-over.
 * Placeholders ({0}, {1}, ...), Minecraft color codes (&c, &l, ...) and the "-nl" line-break marker are
 * swapped for inert tokens before translating and restored after, so DeepL never sees or reorders them
 * incorrectly - but a draft is still a draft, not a merge-ready translation.
 * - any key present in langs/cs.properties but no longer in messages.properties is only reported, never
 * auto-removed - a human should confirm a translation is actually dead before deleting it.
 * <p>
 * Needs a DeepL API key in the DEEPL_API_KEY environment variable (free tier: 500k chars/month, sign up at
 * deepl.com/pro-api - never commit a key, this tool only ever reads it from the environment). Without one
 * set, the tool still runs and reports missing/stale keys, it just can't fill the draft translations in.
 */
public final class MessagesSyncTool {

    private static final String TRANSLATE_MARKER = "#TRANSLATE-DRAFT: ";
    private static final String DEEPL_ENDPOINT = "https://api-free.deepl.com/v2/translate";

    public static void main(String[] args) throws IOException, InterruptedException {
        Path resourceRoot = findResourceRoot();
        Path reference = resourceRoot.resolve("messages.properties");
        Path csFile = resourceRoot.resolve("langs/cs.properties");

        LinkedHashMap<String, String> referenceEntries = parse(reference);
        LinkedHashMap<String, String> csEntries = parse(csFile);
        System.out.println("Reference (messages.properties): " + referenceEntries.size() + " keys");

        List<String> missing = referenceEntries.keySet().stream()
                .filter(k -> !csEntries.containsKey(k))
                .collect(Collectors.toList());
        List<String> stale = csEntries.keySet().stream()
                .filter(k -> !referenceEntries.containsKey(k))
                .collect(Collectors.toList());

        if (!stale.isEmpty())
            System.out.println("cs.properties: " + stale.size() + " key(s) no longer in messages.properties (NOT removed automatically - confirm before deleting) - " + stale);

        if (missing.isEmpty()) {
            System.out.println("cs.properties: already in sync (" + csEntries.size() + " keys)");
            return;
        }

        String apiKey = System.getenv("DEEPL_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("cs.properties: " + missing.size() + " missing key(s), but DEEPL_API_KEY isn't set - nothing translated. Missing: " + missing);
            return;
        }

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        StringBuilder append = new StringBuilder();
        String existing = Files.exists(csFile) ? Files.readString(csFile, StandardCharsets.UTF_8) : "";
        if (!existing.isEmpty() && !existing.endsWith("\n")) append.append('\n');

        int translated = 0;
        for (String key : missing) {
            String english = referenceEntries.get(key);
            String draft;
            try {
                draft = translate(client, apiKey, english);
            } catch (Exception e) {
                System.out.println("  FAILED to translate '" + key + "': " + e.getMessage());
                continue;
            }
            append.append(TRANSLATE_MARKER).append(key).append('\n');
            append.append(key).append("= ").append(draft).append('\n');
            translated++;
        }

        if (translated > 0) {
            Files.writeString(csFile, append.toString(), StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        }
        System.out.println("cs.properties: drafted " + translated + "/" + missing.size()
                + " missing key(s) via DeepL, marked with '" + TRANSLATE_MARKER.trim() + "' - review before relying on them");
    }

    //Swaps {0}/{1}/.../ Minecraft color+format codes (&c, &l, ...)/the "-nl" line-break marker for inert,
    //alphanumeric-only tokens DeepL won't translate or reorder incorrectly, translates, then restores them
    //in the SAME order - correct even if DeepL moves a token to match Czech word order, since each token
    //round-trips to exactly the original text it stood in for.
    private static String translate(HttpClient client, String apiKey, String english) throws IOException, InterruptedException {
        List<String> placeholders = new ArrayList<>();
        String protectedText = protect(english, placeholders);

        String body = "{\"text\":[\"" + jsonEscape(protectedText) + "\"],\"source_lang\":\"EN\",\"target_lang\":\"CS\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPL_ENDPOINT))
                .header("Authorization", "DeepL-Auth-Key " + apiKey)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200)
            throw new IOException("DeepL returned HTTP " + response.statusCode() + ": " + response.body());

        String translatedProtected = extractFirstTranslation(response.body());
        return restore(translatedProtected, placeholders);
    }

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\d+}|&[0-9a-zA-Z]| -nl ");

    private static String protect(String text, List<String> out) {
        Matcher m = PLACEHOLDER_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            out.add(m.group());
            m.appendReplacement(sb, "XPH" + (out.size() - 1) + "X");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static final Pattern RESTORE_PATTERN = Pattern.compile("XPH(\\d+)X");

    private static String restore(String text, List<String> placeholders) {
        Matcher m = RESTORE_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            int idx = Integer.parseInt(m.group(1));
            String original = idx < placeholders.size() ? placeholders.get(idx) : m.group();
            m.appendReplacement(sb, Matcher.quoteReplacement(original));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    //Minimal hand-rolled extraction (no JSON library, matching this tool's zero-dependency goal) - safe
    //here specifically because we always send exactly ONE string, so the response always has exactly one
    //translations[].text to find; a general-purpose JSON response isn't assumed anywhere else.
    private static String extractFirstTranslation(String json) {
        String marker = "\"text\":\"";
        int start = json.indexOf(marker);
        if (start < 0) throw new RuntimeException("Unexpected DeepL response: " + json);
        start += marker.length();
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') break;
            if (c == '\\' && i + 1 < json.length()) {
                char next = json.charAt(++i);
                switch (next) {
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        sb.append((char) Integer.parseInt(json.substring(i + 1, i + 5), 16));
                        i += 4;
                    }
                    default -> sb.append(next);
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static LinkedHashMap<String, String> parse(Path file) throws IOException {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (!Files.exists(file)) return map;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            //Mirrors MessagesFile.loadLine(): split on "= " first (falling back to a bare "="), then strip
            //exactly one further leading space and one surrounding quote pair if present.
            String key, value;
            int sep = line.indexOf("= ");
            if (sep >= 0) {
                key = line.substring(0, sep);
                value = line.substring(sep + 2);
            } else {
                sep = line.indexOf('=');
                if (sep < 0) continue;
                key = line.substring(0, sep);
                value = line.substring(sep + 1);
            }
            key = key.trim();
            if (value.startsWith(" ")) value = value.substring(1);
            if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
                value = value.substring(1, value.length() - 1);
            map.put(key, value);
        }
        return map;
    }

    private static Path findResourceRoot() {
        Path candidate = Path.of("src/main/resources");
        if (Files.isDirectory(candidate) && Files.exists(candidate.resolve("messages.properties"))) return candidate;
        candidate = Path.of("AlixSystemLoader/src/main/resources");
        if (Files.isDirectory(candidate) && Files.exists(candidate.resolve("messages.properties"))) return candidate;
        throw new IllegalStateException("Could not locate AlixSystemLoader's 'src/main/resources' folder from working directory " + Path.of("").toAbsolutePath());
    }
}
