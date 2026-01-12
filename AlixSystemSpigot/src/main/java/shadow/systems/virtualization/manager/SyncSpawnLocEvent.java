package shadow.systems.virtualization.manager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
import shadow.systems.virtualization.manager.SpawnLocEventManager.SpawnEventExecutor;

final class SyncSpawnLocEvent implements Listener {

    private final SpawnEventExecutor exec = new SpawnEventExecutor();

    @SuppressWarnings("removal")
    @EventHandler(priority = EventPriority.MONITOR)
    void onSpawn(PlayerSpawnLocationEvent event) {
        this.exec.onInvocation(event);
    }
}