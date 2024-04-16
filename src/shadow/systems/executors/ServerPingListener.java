package shadow.systems.executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import shadow.systems.login.filters.ServerPingManager;
import shadow.systems.login.result.ConnectionThreadManager;
import shadow.utils.main.AlixUtils;

public final class ServerPingListener implements Listener {

    @EventHandler
    public final void onServerPing(ServerListPingEvent event) {
        if (AlixUtils.antibotService) ConnectionThreadManager.onPingRequest(event.getAddress());
        if (ServerPingManager.isRegistered()) ServerPingManager.add0(event.getAddress().getHostAddress());
    }
}