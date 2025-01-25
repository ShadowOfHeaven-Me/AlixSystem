package alix.common.data.premium.name;

import alix.common.utils.AlixCache;
import com.google.common.cache.Cache;

import java.util.Map;

//Maps premium nicknames that are 16 characters long
public final class PremiumNameManager {

    private static final Map<String, String> premiumNicknamesCache;

    static {
        Cache<String, String> cache = AlixCache.newBuilder().maximumSize(100).build();
        premiumNicknamesCache = cache.asMap();
    }

    public static void mapOverflow(String prefixedName, String originalName) {
        premiumNicknamesCache.put(prefixedName, originalName);
    }

    public static String removeOriginalByPrefixed(String prefixedName) {
        return premiumNicknamesCache.remove(prefixedName);
    }
}