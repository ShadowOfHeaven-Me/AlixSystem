package shadow.systems.executors;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import shadow.utils.world.AlixWorld;

public final class PaperSpawnExecutors implements Listener {

    //spawn in verification world fix
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSpawnChange(PlayerSetSpawnEvent event) {
        Location loc = event.getLocation();
        if (loc != null && loc.getWorld().equals(AlixWorld.CAPTCHA_WORLD)) event.setCancelled(true);
    }
}