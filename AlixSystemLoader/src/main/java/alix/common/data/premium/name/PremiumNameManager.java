package alix.common.data.premium.name;

import alix.common.utils.AlixCache;

import java.util.Map;

//Maps premium nicknames that are 16 characters long
public final class PremiumNameManager {

    private static final Map<String, String> premiumNicknamesCache = AlixCache.newBuilder().maximumSize(100).<String, String>build().asMap();

    public static void mapOverflow(String prefixedName, String originalName) {
        premiumNicknamesCache.put(prefixedName, originalName);
    }

    public static String removeOriginalByPrefixed(String prefixedName) {
        return premiumNicknamesCache.remove(prefixedName);
    }
}