package shadow.systems.virtualization.manager;

import alix.common.utils.other.throwable.AlixError;
import io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent;
import net.kyori.adventure.identity.Identity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

final class AsyncSpawnLocEvent implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    void onSpawn(AsyncPlayerSpawnLocationEvent event) {
        var uuid = event.getConnection().getAudience().pointers().get(Identity.UUID).orElseThrow(() -> new AlixError("Missing Identity.UUID"));
        SpawnLocEventManager.onSpawnLocEvent(uuid, event.getSpawnLocation(), event::setSpawnLocation);
    }
}