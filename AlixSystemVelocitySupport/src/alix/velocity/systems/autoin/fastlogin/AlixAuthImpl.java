package alix.velocity.systems.autoin.fastlogin;

import com.github.games647.fastlogin.core.hooks.AuthPlugin;
import com.velocitypowered.api.proxy.Player;

public class AlixAuthImpl implements AuthPlugin<Player> {

    @Override
    public boolean forceLogin(Player player) {
        return true;
    }

    @Override
    public boolean forceRegister(Player player, String s) {
        return true;
    }

    @Override
    public boolean isRegistered(String s) throws Exception {
        return false;
    }
}