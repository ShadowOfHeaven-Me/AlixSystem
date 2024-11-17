package shadow.systems.login.autoin;

import alix.common.data.premium.PremiumData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PremiumAccountCache {

    private static final Map<String, PremiumData> premiumPlayersCache = new ConcurrentHashMap<>();

    public static PremiumData remove(String username) {
        return premiumPlayersCache.remove(username);
    }

    public static PremiumData get(String username) {
        return premiumPlayersCache.get(username);
    }

    public static void add(String username, PremiumData data) {
        premiumPlayersCache.put(username, data);
    }
}