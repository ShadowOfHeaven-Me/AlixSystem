package alix.velocity.utils.user;

import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserManager {

    private static final Map<UUID, VerifiedUser> USERS = new ConcurrentHashMap<>();

    public static void add(ConnectedPlayer player) {
        var user = new VerifiedUser(player);
        USERS.put(player.getUniqueId(), user);

        user.getChannel().closeFuture().addListener(f -> {
           USERS.remove(player.getUniqueId());
        });
    }

    public static VerifiedUser get(UUID uuid) {
        return USERS.get(uuid);
    }
}