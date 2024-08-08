package alix.common.connection.filters;

import alix.common.connection.ConnectionThreadManager;
import alix.common.messages.Messages;
import alix.common.utils.config.ConfigProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConnectionManager {

    public static final boolean isEnabled = ConfigProvider.config.getBoolean("prevent-first-time-join");
    public static final String preventFirstTimeJoinMessage = Messages.get("prevent-first-time-join");
    //private static final ConcurrentLoopSet<ArrayKey> set;
    public static final Map<String, Long> MAP;
    private static final long forgetInMillis = ConfigProvider.config.getInt("forget-connection-in") * 1000L;

    //TODO: make sure this never overflows (somehow)
    static {
        short maxSize = (short) Math.max(Math.min((short) ConfigProvider.config.getInt("connection-list-size"), 32767), 3);
        MAP = isEnabled ? new ConcurrentHashMap<>(maxSize, 1.0f, 5) : null;
        //set = isEnabled ? new ConcurrentLoopSet<>(maxSize) : null;
    }

    public static boolean disallowJoin(String name) {
        return isEnabled && MAP.put(name, System.currentTimeMillis() + forgetInMillis) == null;
        //return !set.putNext(KeyUtils.key(name));
    }

    private static long nextRemoval, leastTimeVar;

    public static void tick() {
        long now = System.currentTimeMillis();

        if (nextRemoval > now) return;//we know that no entries will need to be removed in this tick, so we just skip it

        leastTimeVar = Long.MAX_VALUE;
        MAP.forEach((n, t) -> {
            if (t < now) MAP.remove(n);
            else if (t < leastTimeVar) leastTimeVar = t;
        });
        if (leastTimeVar != Long.MAX_VALUE)
            nextRemoval = leastTimeVar - now - ConnectionThreadManager.TICK_MILLIS_DELAY;

        /*Iterator<Map.Entry<String, Long>> it = MAP.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Long> e = it.next();
            it.remove();
        }*/
    }
}