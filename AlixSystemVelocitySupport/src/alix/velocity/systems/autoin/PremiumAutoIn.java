package alix.velocity.systems.autoin;

import java.util.HashSet;
import java.util.Set;

public final class PremiumAutoIn {

    private static final AuthAPI auth;
    private static final boolean initialized;
    private static final Set<String> set;

    static {
        auth = AuthAPI.getAuthAPI();
        initialized = auth != null;
        set = initialized ? new HashSet<>() : null;
    }

    public static void add(String name) {
        set.add(name);
    }

    public static boolean remove(String name) {
        return initialized && set.remove(name);
    }

    public static boolean contains(String name) {
        return initialized && set.contains(name);
    }

    public static void checkForInit() {
    }
}