package shadow.systems.executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import alix.common.connection.filters.ServerPingManager;
import alix.common.connection.ConnectionThreadManager;
import shadow.utils.main.AlixUtils;

public final class ServerPingListener implements Listener {

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        if (AlixUtils.antibotService) ConnectionThreadManager.onPingRequest(event.getAddress());
        if (ServerPingManager.isRegistered()) ServerPingManager.add0(event.getAddress().getHostAddress());
    }
}