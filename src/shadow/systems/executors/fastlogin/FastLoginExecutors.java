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

    public FastLoginExecutors() {
    }

    @EventHandler
    public void onPreLogin(BukkitFastLoginPreLoginEvent e) {
        //Main.logError("IS PREMIUM THIS MFO " + e.getProfile().isPremium() + " " + PremiumAutoIn.isPremium(e.getProfile().getId()));

        //Main.logError("IS PREMIUM THIS MFO " + e.getProfile().isPremium());// + " " + e.getProfile().getId() + " " + PremiumAutoIn.isPremium(UserManager.getConnecting(e.getUsername()).getUUID()));


        if (e.getProfile().isPremium()) PremiumAutoIn.add(e.getUsername());
        else {
            PremiumAutoIn.remove(e.getUsername());
        }
    }

/*    @SneakyThrows
    private static boolean isPremium(String name) {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
        HttpsURLConnection co = (HttpsURLConnection) url.openConnection();
    }*/
}