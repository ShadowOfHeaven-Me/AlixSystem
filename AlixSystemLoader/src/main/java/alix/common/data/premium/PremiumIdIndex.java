package alix.common.data.premium;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class PremiumIdIndex {

    private static final Set<UUID> UUIDS = ConcurrentHashMap.newKeySet();

    public static void index(UUID uuid) {
        UUIDS.add(uuid);
    }

    public static boolean isIndexed(UUID uuid) {
        return UUIDS.contains(uuid);
    }
}