package shadow.systems.login.autoin;

import org.bukkit.event.Listener;
import shadow.Main;
import shadow.utils.main.AlixUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PremiumAutoIn {

    private static final AuthAPI auth;
    private static final boolean initialized;
    //private static final ProtocolSupportRegistry protocolSupport;
    private static final Set<String> premiumPlayers;

    static {
        boolean configInit = !AlixUtils.isOnlineModeEnabled && Main.config.getBoolean("premium-auto-in", true);//dictated by the server's online mode status and the config.yml

        auth = configInit ? AuthAPI.getAuthenticatorAPI() : null;//initializes the api if it can find it

        initialized = auth != null;//whether the auth api was found (so auto auth system works)

        premiumPlayers = initialized ? ConcurrentHashMap.newKeySet() : null;//creates a set of players if the auth system works

        if (configInit) {
            if (Main.pm.isPluginEnabled("FastLogin"))
                Main.logInfo("Using FastLogin for automatic premium user authentication.");
            else
                Main.logInfo("FastLogin is recommended for the automatic authentication of premium users. Download link: https://ci.codemc.io/job/Games647/job/FastLogin/changes");
        }
    }

    public static void checkForInit() {

    }

    public static boolean remove(String username) {
        return initialized && premiumPlayers.remove(username);
    }/*

    public static boolean contains(String username) {
        return initialized && premiumPlayers.contains(username);
    }*/

    public static void add(String username) {
        if (initialized) premiumPlayers.add(username);
    }

    public static boolean isPremium(UUID uuid) {
        return initialized && auth.getAuthenticator().isPremium(uuid);
    }

/*    private static ProtocolSupportRegistry getProtocolSupport() {
        if (Main.pm.isPluginEnabled("ProtocolLib")) return new ProtocolLibEvents();
        //if (Main.pm.isPluginEnabled("ProtocolSupport")) return new ProtocolSupportEvents();
        return null;
    }*/

    public static Listener getAutoLoginListener() {
        return initialized ? auth.getListener() : null;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}