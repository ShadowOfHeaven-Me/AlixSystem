package shadow.systems.executors.captcha;

import alix.common.scheduler.AlixScheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import shadow.Main;
import shadow.utils.users.Verifications;
import shadow.utils.world.AlixWorld;

import java.util.concurrent.TimeUnit;

public final class CaptchaRespawnExecutors implements Listener {

    public CaptchaRespawnExecutors(boolean forceRespawn) {
        if (forceRespawn) Main.pm.registerEvents(new ImmediateRespawnExecutors(), Main.plugin);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if (Verifications.has(e.getPlayer())) e.setRespawnLocation(AlixWorld.TELEPORT_LOCATION);
    }

    private static final class ImmediateRespawnExecutors implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onDeath(PlayerDeathEvent e) {
            Player p = e.getEntity();
            if (p.getWorld().equals(AlixWorld.CAPTCHA_WORLD) && Verifications.has(p))//Manually respawn the player if the gamerule is unavailable
                AlixScheduler.runLaterSync(() -> p.spigot().respawn(), 100, TimeUnit.MILLISECONDS);
        }
    }
}