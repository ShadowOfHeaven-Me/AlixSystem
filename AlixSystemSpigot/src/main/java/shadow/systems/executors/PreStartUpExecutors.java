package shadow.systems.executors;

import alix.common.messages.Messages;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public final class PreStartUpExecutors implements Listener {

    private final String msg = Messages.get("server-still-starting");

    //Assumes other Listeners will respect the cancelling
    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, msg);
    }
}