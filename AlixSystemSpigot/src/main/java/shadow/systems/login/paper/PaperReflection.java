package shadow.systems.login.paper;

import alix.common.utils.AlixCommonUtils;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.lang.reflect.Method;
import java.util.UUID;

public final class PaperReflection {

    private static final Method getPlayerProfile, setId;
    private static final boolean available;

    static {
        Method getPlayerProfile0 = null;
        Method setId0 = null;
        boolean available0 = false;
        try {
            getPlayerProfile0 = AsyncPlayerPreLoginEvent.class.getMethod("getPlayerProfile");
            setId0 = getPlayerProfile0.getReturnType().getMethod("setId", UUID.class);
            available0 = true;
        } catch (Throwable ignored) {
        }
        available = available0;
        getPlayerProfile = getPlayerProfile0;
        setId = setId0;
    }


    public static void override(AsyncPlayerPreLoginEvent event, UUID uuid) {
        try {
            var profile = getPlayerProfile.invoke(event);
            setId.invoke(profile, uuid);
        } catch (Exception e) {
            AlixCommonUtils.logException(e);
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    /*@EventHandler
    void onHandshake(PlayerHandshakeEvent event) {

    }*/
}