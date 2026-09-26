package alix.common.connection.filters;

import alix.common.messages.Messages;
import alix.common.utils.AlixCache;
import alix.common.utils.config.ConfigProvider;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class ConnectionManager {

    public static final boolean isEnabled = ConfigProvider.config.getBoolean("prevent-first-time-join-during-high-traffic");
    public static final String preventFirstTimeJoinMessage = Messages.get("prevent-first-time-join");
    //private static final ConcurrentLoopSet<ArrayKey> set;
    //FUNCTIONALITY (audit, 2026-09-24): clamped to a 1-second floor - unlike maxSize right below, this was
    //previously unclamped, and AlixCache's expireAfterWrite() (backed by Guava's CacheBuilder) throws
    //IllegalArgumentException for a negative/zero duration, which - since this static initializer runs
    //during class loading, touched by connection filtering at startup - crashed the whole plugin's boot
    //with an ExceptionInInitializerError for any admin who set this to 0 or a negative value (the same
    //"0 or less = disable" convention this file's own isEnabled/config already documents elsewhere).
    private static final long forgetInMillis = Math.max(1, ConfigProvider.config.getInt("forget-connection-in")) * 1000L;
    private static final Map<String, Boolean> CACHE;

    static {
        if (!isEnabled) {
            CACHE = null;
        } else {
            int maxSize = Math.max(Math.min(ConfigProvider.config.getInt("connection-list-size"), 32767), 3);
            CACHE = AlixCache.newBuilder().expireAfterWrite(forgetInMillis, TimeUnit.MILLISECONDS).maximumSize(maxSize).<String, Boolean>build().asMap();
        }
        //CACHE = isEnabled ? (Map<String, Long>) CacheBuilder.newBuilder().expireAfterWrite(forgetInMillis, TimeUnit.MILLISECONDS).maximumSize(maxSize).build().asMap() : null;
        //set = isEnabled ? new ConcurrentLoopSet<>(maxSize) : null;
    }

    public static boolean disallowJoin(String name) {
        return isEnabled && CACHE.put(name, Boolean.TRUE) == null;
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