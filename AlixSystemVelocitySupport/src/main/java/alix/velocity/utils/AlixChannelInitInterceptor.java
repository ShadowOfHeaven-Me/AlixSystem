package alix.velocity.utils;


import alix.common.utils.other.throwable.AlixError;
import alix.velocity.systems.channel.ServerChannelInitializer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.network.ConnectionManager;

import java.lang.reflect.Field;

public final class AlixChannelInitInterceptor {

    public static void initializeInterceptor(VelocityServer server) {
        try {
            for (Field f : server.getClass().getDeclaredFields()) {
                if (f.getType() == ConnectionManager.class) {
                    f.setAccessible(true);
                    ConnectionManager m = (ConnectionManager) f.get(server);
                    var serverChannel = m.getServerChannelInitializer();

                    //ChannelInitializer<?> initializer = m.getServerChannelInitializer().get();
                    //if (initializer.getClass() != AlixChannelInitializer.EXTENDING_CLASS) throw new AlixException("Report this immediately! Server class: '" + initializer.getClass() + "' with '" + AlixChannelInitializer.EXTENDING_CLASS + "' registered in Alix!");
                    serverChannel.set(new ServerChannelInitializer(server, serverChannel.get()));
                    break;
                }
            }
        } catch (Exception e) {
            throw new AlixError(e);
        }
    }

    /*public static VelocityConnectionFilter[] getConnectionFilters() {
        List<ConnectionFilter> list = new ArrayList<>();
        list.add(GeoIPTracker.INSTANCE);
        list.add(AntiVPN.INSTANCE);

        return list.stream().map(ConnectionFilterAdapter::new).toArray(VelocityConnectionFilter[]::new);
    }*/

    /*public static void handleJoin(ConnectedPlayer player) {
        if (PremiumAutoIn.remove(player.getUsername())) return;//leave him be and let him join the server

        PersistentUserData data = UserDataManager.get(player.getUsername());

        Verifications.add(player, data);
    }*/
}