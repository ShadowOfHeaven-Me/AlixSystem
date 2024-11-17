package alix.common.connection.filters;

import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ConnectionManager {

    public static final boolean isEnabled = ConfigProvider.config.getBoolean("prevent-first-time-join");
    public static final String preventFirstTimeJoinMessage = Messages.get("prevent-first-time-join");
    //private static final ConcurrentLoopSet<ArrayKey> set;
    private static final long forgetInMillis = ConfigProvider.config.getInt("forget-connection-in") * 1000L;
    public static final Map<String, Long> CACHE;

    static {
        if (!isEnabled) {
            CACHE = null;
        } else {
            int maxSize = Math.max(Math.min(ConfigProvider.config.getInt("connection-list-size"), 32767), 3);
            Cache<String, Long> cache = CacheBuilder.newBuilder().expireAfterWrite(forgetInMillis, TimeUnit.MILLISECONDS).maximumSize(maxSize).build();
            CACHE = cache.asMap();
        }
        //CACHE = isEnabled ? (Map<String, Long>) CacheBuilder.newBuilder().expireAfterWrite(forgetInMillis, TimeUnit.MILLISECONDS).maximumSize(maxSize).build().asMap() : null;
        //set = isEnabled ? new ConcurrentLoopSet<>(maxSize) : null;
    }

    public static boolean disallowJoin(String name) {
        return isEnabled && CACHE.put(name, System.currentTimeMillis() + forgetInMillis) == null;
        //return !set.putNext(KeyUtils.key(name));
    }

    //private static long nextRemoval, leastTimeVar;

    /*public static void tick() {
        long now = System.currentTimeMillis();

        if (nextRemoval > now) return;//we know that no entries will need to be removed in this tick, so we just skip it

        leastTimeVar = Long.MAX_VALUE;
        MAP.forEach((n, t) -> {
            if (t < now) MAP.remove(n);
            else if (t < leastTimeVar) leastTimeVar = t;
        });
        if (leastTimeVar != Long.MAX_VALUE)
            nextRemoval = leastTimeVar - now - ConnectionThreadManager.TICK_MILLIS_DELAY;

        *//*Iterator<Map.Entry<String, Long>> it = MAP.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            it.remove();
        }*//*
    }*/
}