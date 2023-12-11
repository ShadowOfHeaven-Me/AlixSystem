package alix.velocity.utils;

import alix.common.antibot.connection.ConnectionFilter;
import alix.common.antibot.connection.filters.AntiVPN;
import alix.common.antibot.connection.filters.ConnectionManager;
import alix.common.antibot.connection.filters.GeoIPTracker;
import alix.common.antibot.connection.filters.ServerPingManager;

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