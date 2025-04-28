package shadow.systems.executors;

import alix.common.connection.filters.ServerPingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

public final class ServerPingListener implements Listener {

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        //if (AlixUtils.antibotService) ConnectionThreadManager.onConnection(event.getAddress());
        if (ServerPingManager.isRegistered()) ServerPingManager.add0(event.getAddress().getHostAddress());
    }
}