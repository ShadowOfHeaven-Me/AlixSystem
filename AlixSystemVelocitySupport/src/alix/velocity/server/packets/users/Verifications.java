package alix.velocity.server.packets.users;

import alix.velocity.systems.captcha.CaptchaManager;
import alix.velocity.server.packets.users.UnverifiedUser;
import alix.velocity.utils.data.PersistentUserData;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Verifications {

    private static final Map<UUID, UnverifiedUser> map = new ConcurrentHashMap<>();

    public static void add(ConnectedPlayer player, PersistentUserData data) {
        map.put(player.getUniqueId(), new UnverifiedUser(player, data));
    }

    public static void remove(Player player) {
        map.remove(player.getUniqueId());
    }
}