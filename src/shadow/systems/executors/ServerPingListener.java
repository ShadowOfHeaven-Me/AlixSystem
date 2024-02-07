package shadow.systems.executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import shadow.systems.login.filters.ServerPingManager;

public final class ServerPingListener implements Listener {

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        ServerPingManager.pureAdd(event.getAddress().getHostAddress());
    }
}