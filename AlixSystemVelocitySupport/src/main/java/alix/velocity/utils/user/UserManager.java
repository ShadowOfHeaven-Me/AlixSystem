package alix.velocity.utils.user;

import alix.common.reflection.CommonReflection;
import alix.velocity.Main;
import alix.velocity.systems.commands.executable.ExecutableCommandList;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import io.netty.channel.Channel;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserManager {

    private static final Map<UUID, VerifiedUser> USERS = new ConcurrentHashMap<>();
    private static final Map<String, Channel> CONNECTED_USERS = new ConcurrentHashMap<>();// new MapMaker().weakValues().makeMap();

    @SneakyThrows
    public static void init(ProxyServer server) {
        var config = server.getConfiguration();
        var f = CommonReflection.getDeclaredFieldAccessible(config.getClass(), "onlineModeKickExistingPlayers");
        boolean kickExisting = (boolean) f.get(config);
        if (!kickExisting) {
            f.set(config, true);
            Main.logInfo("Overriding velocity.toml config param of 'kick-existing-players' from false to true, using Alix's system instead, to ensure correct join mechanic");
        }
    }

    public static boolean addConnected(String username, Channel channel) {
        if (!channel.isOpen())
            return false;
        //intentional identity check
        return channel == CONNECTED_USERS.compute(username, (u, c) -> {
            if (c != null && c.isOpen())
                return c;

            channel.closeFuture().addListener(f -> {
                CONNECTED_USERS.remove(username);
                /*AlixCommonMain.logError("INFORMATIVE ERROR");
                new AlixException().printStackTrace();*/
            });
            //try to account for the above statement already executing
            return channel.isOpen() ? channel : null;
        });
    }

    public static void add(ConnectedPlayer player) {
        var user = new VerifiedUser(player);
        USERS.put(player.getUniqueId(), user);

        user.getChannel().closeFuture().addListener(f -> {
            USERS.remove(player.getUniqueId());
        });
        ExecutableCommandList.executeFor(user);
    }

    public static boolean hasVerified(UUID uuid) {
        return USERS.containsKey(uuid);
    }

    public static VerifiedUser getVerified(UUID uuid) {
        return USERS.get(uuid);
    }
}