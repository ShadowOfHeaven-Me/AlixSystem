package alix.common.data.premium;

import alix.common.utils.AlixCache;
import com.google.common.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class PremiumDataCache {

    private static final Map<String, PremiumData> premiumPlayersCache;

    static {
        Cache<String, PremiumData> cache = AlixCache.newBuilder().maximumSize(200).expireAfterWrite(10, TimeUnit.MINUTES).build();
        premiumPlayersCache = cache.asMap();
    }

    public static PremiumData removeOrUnknown(String username) {
        PremiumData data = premiumPlayersCache.remove(username);
        return data != null ? data : PremiumData.UNKNOWN;
    }

    @NotNull
    public static PremiumData getOrUnknown(String username) {
        return premiumPlayersCache.getOrDefault(username, PremiumData.UNKNOWN);
    }

    public static void add(String username, PremiumData data) {
        premiumPlayersCache.put(username, data);
    }

    /*public static PremiumData getCachedOrSet(String username, ClientPublicKey publicKey, UUID uuid, PersistentUserData data) {
        PremiumData currentData = data != null ? data.getPremiumData() : PremiumData.UNKNOWN;
        if (currentData.getStatus().isKnown()) return currentData;

        PremiumData cached = getOrUnknown(username);
        if (cached.getStatus().isKnown()) return cached;

        if (!PremiumSetting.requirePremium(data, uuid, publicKey)) return PremiumData.UNKNOWN;
        PremiumData newData = PremiumUtils.fetchPremiumData(username);

        //cache if the status is known
        if (newData.getStatus().isKnown()) add(username, newData);
        return newData;
    }*/
}