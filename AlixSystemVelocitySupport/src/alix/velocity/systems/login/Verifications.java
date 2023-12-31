package alix.velocity.systems.login;

import alix.velocity.utils.users.UnverifiedUser;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Verifications {

    private static final Map<UUID, UnverifiedUser> map = new ConcurrentHashMap<>();

    public static void add(ConnectedPlayer player) {
        UnverifiedUser user = new UnverifiedUser(player);
    }
}