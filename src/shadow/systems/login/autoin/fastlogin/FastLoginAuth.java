package shadow.systems.login.autoin.fastlogin;

import com.github.games647.fastlogin.bukkit.FastLoginBukkit;
import com.github.games647.fastlogin.core.PremiumStatus;
import org.bukkit.plugin.java.JavaPlugin;
import shadow.systems.login.autoin.LoginAuthenticator;

import java.util.UUID;

public final class FastLoginAuth implements LoginAuthenticator {

    private final FastLoginBukkit base;

    public FastLoginAuth() {
        this.base = (FastLoginBukkit) JavaPlugin.getProvidingPlugin(FastLoginBukkit.class);
        this.base.getCore().setAuthPluginHook(new AlixAuthFastLoginImpl());
    }

    @Override
    public boolean isPremium(UUID uuid) {
        //Main.logError("STATUS: " + base.getStatus(uuid) + " all " + base.getPremiumPlayers());
        return base.getStatus(uuid) == PremiumStatus.PREMIUM;
    }
}