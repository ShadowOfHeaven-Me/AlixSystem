package alix.velocity.utils;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.antibot.connection.filters.AntiVPN;
import alix.common.antibot.connection.filters.ConnectionManager;
import alix.common.antibot.connection.filters.GeoIPTracker;
import alix.common.antibot.connection.filters.ServerPingManager;
import alix.velocity.utils.users.UnverifiedUser;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;

import java.util.ArrayList;
import java.util.List;

public final class AlixHandler {

    public static ConnectionFilter[] getConnectionFilters() {
        ServerInfo
        List<ConnectionFilter> list = new ArrayList<>();
        list.add(new ConnectionManager());
        list.add(new ServerPingManager());
        list.add(new GeoIPTracker());
        list.add(new AntiVPN());
        return list.toArray(new ConnectionFilter[0]);
    }

    public static UnverifiedUser handleOfflineUserJoin(ConnectedPlayer player) {
        return new UnverifiedUser(player);
    }
}