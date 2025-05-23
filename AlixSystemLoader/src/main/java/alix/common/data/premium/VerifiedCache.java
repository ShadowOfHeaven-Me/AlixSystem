package alix.common.data.premium;

import alix.common.utils.AlixCache;
import com.github.retrooper.packetevents.protocol.player.User;
import com.google.common.cache.Cache;

import java.util.Map;

public final class VerifiedCache {

    private static final Map<String, User> verifiedNamesTempCache;

    static {
        Cache<String, User> cache = AlixCache.newBuilder().maximumSize(100).weakValues().build();
        verifiedNamesTempCache = cache.asMap();
    }

    public static void verify(String nickname, User user) {
        verifiedNamesTempCache.put(nickname, user);
    }

    public static boolean removeAndCheckIfEquals(String nickname, User user) {
        return user.equals(verifiedNamesTempCache.remove(nickname));
    }
}