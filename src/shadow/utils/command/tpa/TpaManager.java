package shadow.utils.command.tpa;

import shadow.utils.users.types.VerifiedUser;
import shadow.utils.users.UserManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class TpaManager {

    private static final Map<String, TpaRequest> map = new HashMap<>();

    public static void addRequest(String fromName, Player from, Player to) {
        map.put(fromName, new TpaRequest(from, to, fromName));
    }

    public static void removeRequest(String from) {
        map.remove(from);
    }

    public static TpaRequest getRequest(String from) {
        return map.get(from);
    }

    public static boolean hasRequestsOff(Player p) {
        VerifiedUser u = UserManager.getNullableVerifiedUser(p);
        return u != null && !u.canReceiveTeleportRequests();
    }
}