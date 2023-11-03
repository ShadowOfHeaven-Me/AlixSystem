package shadow.systems.executors.captcha;

import alix.common.scheduler.impl.AlixScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import shadow.systems.login.Verifications;
import shadow.utils.world.AlixWorld;

import java.util.concurrent.TimeUnit;

public final class CaptchaRespawnExecutors implements Listener {

    private final boolean forceRespawn;

    public CaptchaRespawnExecutors(boolean forceRespawn) {
        this.forceRespawn = forceRespawn;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        if (!this.forceRespawn) return;
        Player p = e.getEntity();
        if (p.getWorld().equals(AlixWorld.CAPTCHA_WORLD) && Verifications.has(p)) {//Manually respawn the player if the gamerule is unavailable
            AlixScheduler.runLaterSync(() -> p.spigot().respawn(), 100, TimeUnit.MILLISECONDS);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if (Verifications.has(e.getPlayer())) {
            e.setRespawnLocation(AlixWorld.TELEPORT_LOCATION);
        }
    }
}