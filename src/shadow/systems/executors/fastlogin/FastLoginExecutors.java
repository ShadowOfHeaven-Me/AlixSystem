/*
package shadow.systems.executors.fastlogin;

import com.github.games647.fastlogin.bukkit.event.BukkitFastLoginPreLoginEvent;
import com.github.games647.fastlogin.core.storage.StoredProfile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import shadow.Main;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.systems.login.autoin.PremiumSetting;

import static shadow.systems.login.autoin.PremiumUtils.isNamePremium;

public final class FastLoginExecutors implements Listener {

*/
/*    @EventHandler
    public void onAutoLogin(BukkitFastLoginAutoLoginEvent e) {
        UnverifiedUser u = Verifications.get(e.getProfile().getId());
        JavaUtils.broadcastFast0("AutoLoginEvent: " + e.getProfile() + " " + e.getSession() + " " + (u != null));
    }*//*


    public FastLoginExecutors() {
    }

    @EventHandler
    public void onPreLogin(BukkitFastLoginPreLoginEvent e) {
        //Main.logError("IS PREMIUM THIS MFO " + e.getProfile().isPremium() + " " + PremiumAutoIn.isPremium(e.getProfile().getId()));

        String name = e.getUsername();
        StoredProfile profile = e.getProfile();
        boolean premium = profile.isOnlinemodePreferred();
        //Main.debug("IS THIS MFO PREMIUM " + e.getProfile().isPremium() + " HTTP " + isNamePremium(name) + " UUID GOT " + e.getProfile().getId() + " ROW ID " + e.getProfile().getRowId());// + " STORED STATUS: " + PremiumAutoIn.isPremium(e.getProfile().getId()));// + " " + e.getProfile().getId() + " " + PremiumAutoIn.isPremium(UserManager.getConnecting(e.getUsername()).getUUID()));

        if (!premium && PremiumSetting.requirePremium(name) && isNamePremium(name)) {
            Main.debug("NAME PREMIUM TRUE");
            profile.setOnlinemodePreferred(true);
            premium = true;
        }

        if (premium) PremiumAutoIn.add(name);
        else PremiumAutoIn.remove(name);
    }
}*/
