package shadow.systems.executors.captcha;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import shadow.utils.users.Verifications;
import shadow.utils.users.types.UnverifiedUser;

public final class CaptchaRespawnExecutors implements Listener {

    //currently unused
    public CaptchaRespawnExecutors(boolean forceRespawn) {
        //if (forceRespawn) Main.pm.registerEvents(new ImmediateRespawnExecutors(), Main.plugin);
    }

    //Moved to OfflineExecutors
/*    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent e) {
        if (Verifications.has(e.getPlayer())) e.setRespawnLocation(AlixWorld.TELEPORT_LOCATION);
    }*/


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        UnverifiedUser user = Verifications.get(p);
        if (user != null) user.onSyncDeath();
    }
}