package alix.common.connection.filters;

import alix.common.utils.config.ConfigProvider;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerNameIndex {

    private static final boolean allowCaseSensitive = ConfigProvider.config.getBoolean("allow-case-sensitive-nicknames");
    private static final Map<String, String> MAP = allowCaseSensitive ? null : new ConcurrentHashMap<>();
    //lowercase -> original cased

    public static void index(String name) {
        if (allowCaseSensitive)
            return;

        MAP.put(lower(name), name);
    }

    //TODO: Also disallow during register
    public static boolean isIndexedWithDifferentCase(String s) {
        String result;
        return !allowCaseSensitive && (result = MAP.get(lower(s))) != null && !s.equals(result);
    }

    public static void remove(String name) {
        if (allowCaseSensitive)
            return;

        MAP.remove(lower(name));
    }

    private static String lower(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}