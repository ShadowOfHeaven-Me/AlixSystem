package alix.velocity.utils;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.antibot.connection.types.AntiVPN;
import alix.common.antibot.connection.types.ConnectionManager;
import alix.common.antibot.connection.types.GeoIPTracker;
import alix.common.antibot.connection.types.ServerPingManager;

import java.util.ArrayList;
import java.util.List;

public class AlixHandler {

    public static ConnectionFilter[] getConnectionFilters() {
        List<ConnectionFilter> list = new ArrayList<>();
        list.add(new ConnectionManager());
        list.add(new ServerPingManager());
        list.add(new GeoIPTracker());
        list.add(new AntiVPN());
        return list.toArray(new ConnectionFilter[0]);
    }
}