package shadow.systems.login.autoin;

import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.utils.other.annotation.OptimizationCandidate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;
import shadow.systems.login.autoin.premium.ClientPublicKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PremiumAccountCache {

    @OptimizationCandidate
    private static final Map<String, PremiumData> premiumPlayersCache;

    static {
        Cache<String, PremiumData> cache = CacheBuilder.newBuilder().maximumSize(500).expireAfterWrite(1, TimeUnit.MINUTES).build();
        premiumPlayersCache = cache.asMap();
    }

    public static PremiumData remove(String username) {
        return premiumPlayersCache.remove(username);
    }

    @NotNull
    public static PremiumData getOrUnknown(String username) {
        return premiumPlayersCache.getOrDefault(username, PremiumData.UNKNOWN);
    }

    public static void add(String username, PremiumData data) {
        premiumPlayersCache.put(username, data);
    }

    public static PremiumData getCachedOrSet(String username, ClientPublicKey publicKey, UUID uuid, PersistentUserData data) {
        PremiumData currentData = data != null ? data.getPremiumData() : PremiumData.UNKNOWN;
        if (currentData.getStatus().isKnown()) return currentData;

        PremiumData cached = getOrUnknown(username);
        if (cached.getStatus().isKnown()) return cached;

        if (!PremiumSetting.requirePremium(data, uuid, publicKey)) return PremiumData.UNKNOWN;
        PremiumData newData = PremiumUtils.getPremiumData(username);

        //cache if the status is known
        if (newData.getStatus().isKnown()) add(username, newData);
        return newData;
    }
}