package shadow.systems.executors;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import shadow.utils.world.AlixWorld;

public final class SpigotSpawnExecutors implements Listener {

    //spawn in verification world fix
    @SuppressWarnings("removal")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnChange(PlayerSpawnChangeEvent event) {
        if (event.getNewSpawn().getWorld().equals(AlixWorld.CAPTCHA_WORLD)) event.setCancelled(true);
    }
}