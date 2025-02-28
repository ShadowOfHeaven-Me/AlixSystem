package alix.common.utils.config.alix;

import alix.common.AlixCommonMain;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.file.AlixFileManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Function;

public final class AlixYamlConfig {

    private final AlixYamlConfigImpl impl;

    public AlixYamlConfig() {
        File file = AlixFileManager.getOrCreatePluginFile("config.yml", AlixFileManager.FileType.CONFIG);
        this.impl = new AlixYamlConfigImpl(file);
        this.impl.loadConfig();
    }

    @NotNull
    public String get(String path, @NotNull String def) {
        String val = this.impl.values.get(path);
        if (val == null)
            AlixCommonMain.logWarning("Config " + path + " param not found");
        return val != null ? val.trim() : def;
    }

    public String get(String path) {
        return this.get(path, "");
    }

    public String getString(String path) {
        return removeQuotations(this.get(path));
    }

    private static String removeQuotations(String str) {
        if (str.isEmpty()) return str;
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
        return parsed != null ? parsed : 0;
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
}