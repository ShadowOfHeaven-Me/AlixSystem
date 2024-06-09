package shadow.systems.executors.fastlogin;

import com.github.games647.fastlogin.bukkit.event.BukkitFastLoginPreLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import shadow.systems.login.autoin.PremiumAutoIn;

public final class FastLoginExecutors implements Listener {

/*    @EventHandler
    public void onAutoLogin(BukkitFastLoginAutoLoginEvent e) {
        UnverifiedUser u = Verifications.get(e.getProfile().getId());
        JavaUtils.broadcastFast0("AutoLoginEvent: " + e.getProfile() + " " + e.getSession() + " " + (u != null));
    }*/

    @EventHandler
    public void onPreLogin(BukkitFastLoginPreLoginEvent e) {
        if (e.getProfile().isPremium()) PremiumAutoIn.add(e.getUsername());
        else PremiumAutoIn.remove(e.getUsername());
    }
}