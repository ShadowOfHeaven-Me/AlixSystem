package alix.velocity.utils;


import alix.common.utils.other.throwable.AlixError;
import alix.common.utils.other.throwable.AlixException;
import alix.velocity.systems.filters.ConnectionFilter;
import alix.velocity.systems.filters.firewall.AlixChannelFireWall;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;
import io.netty.channel.ChannelInitializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public final class AlixChannelInitInterceptor {

    public static void initializeInterceptor(VelocityServer server) {
        try {
            for (Field f : server.getClass().getDeclaredFields()) {
                if (f.getType() == ConnectionManager.class) {
                    f.setAccessible(true);
                    ConnectionManager m = (ConnectionManager) f.get(server);
                    ChannelInitializer<?> initializer = m.getServerChannelInitializer().get();
                    if (initializer.getClass() != AlixChannelFireWall.EXTENDING_CLASS)
                        throw new AlixException("Report this immediately! Server class: '" + initializer.getClass() + "' with '" + AlixChannelFireWall.EXTENDING_CLASS + "' registered in Alix!");
                    m.getServerChannelInitializer().set(new AlixChannelFireWall(server));
                    break;
                }
            }
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    public static ConnectionFilter[] getConnectionFilters() {
        List<ConnectionFilter> list = new ArrayList<>();

        return list.toArray(new ConnectionFilter[0]);
    }

    /*public static void handleJoin(ConnectedPlayer player) {
        if (PremiumAutoIn.remove(player.getUsername())) return;//leave him be and let him join the server

        PersistentUserData data = UserDataManager.get(player.getUsername());

        Verifications.add(player, data);
    }*/
}