package alix.common.utils.config.alix;

import alix.common.AlixCommonMain;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.file.AlixFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AlixYamlConfig {

    private final AlixYamlConfigFile file;

    public AlixYamlConfig(File file) {
        this.file = new AlixYamlConfigFile(file);
        this.file.loadConfig();
    }

    public static AlixYamlConfig getOrCreatePluginFile(String name, AlixFileManager.FileType type) {
        return new AlixYamlConfig(AlixFileManager.getOrCreatePluginFile(name, type));
    }

    /**
     * v1.5.2 ("/as reload"): re-reads every AlixYamlConfig-backed file (config.yml, database.yml,
     * email-config.yml, gui-menus/*.yml, ...) currently loaded from disk, in place - see
     * AlixYamlConfigFile#reload() for why a naive re-parse would have duplicated list-type keys.
     * <p>
     * Note this only refreshes what {@link #get}/{@link #getString}/{@link #getBoolean}/etc. return on
     * their NEXT call - a value already copied out into a {@code final} field at startup (several
     * settings classes do this, e.g. EmailConfig) won't change until the affected file is closed and
     * reopened, i.e. a real restart. Call sites that read the config live, at the point of use, pick up
     * the new value immediately.
     */
    public static void reloadAll() {
        AlixYamlConfigFile.reloadAll();
    }

    @NotNull
    public String get(String path, @NotNull String def) {
        String val = this.file.values.get(path);
        if (val == null) {
            List<String> list = this.file.lists.get(path);
            //can't really have an empty list here, but whatever
            if (list == null || list.isEmpty())
                AlixCommonMain.logWarning("Config '" + path + "' param not found");
            else {
                val = list.get(0);
                AlixCommonMain.logWarning("Config '" + path + "' param has a list-like structure " + list + ", using " + path + "=" + val);
            }
        }
        return val != null ? unwrapDefaultOf(val.trim()) : def;
    }

    public String get(String path) {
        return this.get(path, "");
    }

    @NotNull
    public <T> List<T> getList(String path, Function<String, T> transformer) {
        List<String> list = this.file.lists.get(path);
        if (list == null || list.isEmpty()) {//again, can't be empty
            String val = this.file.values.get(path);

            if (val == null) {
                AlixCommonMain.logWarning("Config '" + path + "' param not found");
                list = List.of();
            } else if (!val.isBlank()) {
                //tolerate
                AlixCommonMain.logWarning("List parameter '" + path + "' was defined as a singular, non-list item instead!");
                list = List.of(val);
            } else
                list = List.of();
        }
        List<T> transformed = new ArrayList<>(list.size());
        for (String s : list)
            transformed.add(transformer.apply(unwrapDefaultOf(s)));
        return transformed;
    }

    public List<String> getStringList(String path) {
        return this.getList(path, AlixYamlConfig::removeQuotations);
    }

    public String getString(String path, String def) {
        return removeQuotations(this.get(path, def));
    }

    public String getString(String path) {
        return this.getString(path, "");
    }

    /**
     * Like {@link #getString(String, String)}, but never logs a "param not found" warning when the key
     * is absent. Intended for genuinely optional keys (e.g. a menu item's optional lore/head-texture/
     * item-model/custom-model-data/internal fields, where most items simply don't set most of them) -
     * using the warning-logging variant there would spam the console on every first load of a menu,
     * since "not set" is the normal, expected case for those keys, not a config mistake.
     */
    @NotNull
    public String getStringQuiet(String path, @NotNull String def) {
        String val = this.file.values.get(path);
        if (val == null) {
            List<String> list = this.file.lists.get(path);
            if (list != null && !list.isEmpty()) val = list.get(0);
        }
        return val != null ? removeQuotations(unwrapDefaultOf(val.trim())) : def;
    }

    /**
     * Quiet counterpart to {@link #getInt(String, int)} - see {@link #getStringQuiet(String, String)}.
     */
    public int getIntQuiet(String path, int def) {
        String value = this.getStringQuiet(path, String.valueOf(def));
        Integer parsed = this.parse(value, Integer::parseInt);
        if (parsed != null) return parsed;
        parsed = this.parse(AlixCommonUtils.getNumbersOnly(value.split("\\.")[0]), Integer::parseInt);
        return parsed != null ? parsed : def;
    }

    /**
     * Quiet counterpart to {@link #getStringList(String)} - see {@link #getStringQuiet(String, String)}.
     */
    @NotNull
    public List<String> getStringListQuiet(String path) {
        List<String> list = this.file.lists.get(path);
        if (list == null || list.isEmpty()) {
            String val = this.file.values.get(path);
            list = val == null ? List.of() : List.of(val);
        }
        List<String> transformed = new ArrayList<>(list.size());
        for (String s : list)
            transformed.add(removeQuotations(unwrapDefaultOf(s)));
        return transformed;
    }

    /**
     * True if this config file has a value or list explicitly set for the given key (as opposed to it
     * being absent and any lookup falling back to a default). Useful to distinguish "not configured" from
     * "configured to the same value as the default" when that distinction matters.
     */
    public boolean hasKey(String path) {
        return this.file.values.containsKey(path) || this.file.lists.containsKey(path);
    }

    private static final Pattern DEFAULT_OF_PATTERN = Pattern.compile("^default_of\\((.*)\\)$", Pattern.DOTALL);

    /**
     * A value written as {@code default_of(x)} is read exactly as {@code x} - the wrapper only matters
     * to {@link alix.common.utils.file.update.FileUpdater}'s on-disk merge, which uses it to tell "the
     * bundled default, never touched by the operator" apart from "the operator deliberately set this",
     * so a future plugin update can silently change the former (e.g. reposition a gui-menus item, swap a
     * default icon) without touching the latter. See the "DEFAULT_OF(...) VALUES" section in
     * gui-menus/*.yml for the operator-facing explanation. Stripping the wrapper here, at every read,
     * means call sites never need to know it exists.
     */
    private static String unwrapDefaultOf(String value) {
        if (value == null) return null;
        Matcher m = DEFAULT_OF_PATTERN.matcher(value.trim());
        return m.matches() ? m.group(1).trim() : value;
    }

    private static String removeQuotations(String str) {
        if (str.length() <= 1) return str;
        char first = str.charAt(0);
        if (first == '"') {
            char last = str.charAt(str.length() - 1);
            if (last == '"') return str.substring(1, str.length() - 1);
            else {
                AlixCommonMain.logWarning("Opened quotation marks not closed - (" + str + ")!");
                return str.substring(1);
            }
        }
        return str;
    }

    private <T> T parse(String value, Function<String, T> parser) {
        try {
            return parser.apply(value);
        } catch (Exception e) {
            return null;
        }
    }

    private <T extends Number> Number parseNumber(String path, Function<String, T> parser) {
        return this.parseNumber(path, parser, 0);
    }

    private <T extends Number> Number parseNumber(String path, Function<String, T> parser, Number def) {
        String value = this.get(path, String.valueOf(def));

        T parsed = this.parse(value, parser);
        if (parsed != null) return parsed;

        //todo: make this support '.' decimal places
        parsed = this.parse(AlixCommonUtils.getNumbersOnly(value.split("\\.")[0]), parser);
        return parsed != null ? parsed : def;
    }

    public double getDouble(String path) {
        return this.parseNumber(path, Double::parseDouble).doubleValue();
    }

    public float getFloat(String path) {
        return this.parseNumber(path, Float::parseFloat).floatValue();
    }

    public long getLong(String path) {
        return this.parseNumber(path, Long::parseLong).longValue();
    }

    public int getInt(String path) {
        return this.getInt(path, 0);
    }

    public int getInt(String path, int def) {
        return this.parseNumber(path, Integer::parseInt, def).intValue();
    }

    public short getShort(String path) {
        return this.parseNumber(path, Short::parseShort).shortValue();
    }

    public byte getByte(String path) {
        return this.parseNumber(path, Byte::parseByte).byteValue();
    }

    public boolean getBoolean(String path) {
        return this.getBoolean(path, false);
    }

    public boolean getBoolean(String path, boolean def) {
        return Boolean.parseBoolean(this.get(path, Boolean.toString(def)));
    }

    public File getFile() {
        return file.getFile();
    }
}