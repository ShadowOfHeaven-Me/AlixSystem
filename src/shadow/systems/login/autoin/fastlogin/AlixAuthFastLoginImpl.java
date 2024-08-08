package shadow.systems.login.autoin.fastlogin;

import com.github.games647.fastlogin.core.hooks.AuthPlugin;
import org.bukkit.entity.Player;
import alix.common.data.file.UserFileManager;

//This implementation does nothing except showing
//FastLogin that an authentication plugin is
//present. The FastLogin support implementation itself
//is present in the PremiumAutoIn class
public final class AlixAuthFastLoginImpl implements AuthPlugin<Player> {

    @Override
    public boolean forceLogin(Player player) {
        //Main.logError("forceLogin");
        return true;
    }

    @Override
    public boolean forceRegister(Player player, String s) {
        //Main.logError("forceRegister");
        return true;
    }

    @Override
    public boolean isRegistered(String s) {
        //Main.logError("isRegistered");
        return UserFileManager.hasName(s);
    }

    /*@Override
    public boolean forceLogin(Player player) {

        UnverifiedUser user = Verifications.get(player);

        if (user == null) {

            User verifiedUser = UserManager.getNullableUserOnline(player);

            if (verifiedUser == null) UserManager.addOnlineUser(player);

            return true;
        }

        if (!user.isRegistered())
            return register(user, Password.createRandomUnhashed());//'forceRegister' is never invoked, so we have to do it manually - it's buggy I guess

        Main.logInfo("Auto-logged player " + user.getPlayer().getName());

        JavaScheduler.sync(user::autoLogin);
        //JavaScheduler.sync(() -> UserManager.addOnlineUser(player));
        return true;
    }

    @Override
    public boolean forceRegister(Player player, String something) {
        return register(Verifications.get(player), Password.createRandomUnhashed());
    }

    private boolean register(UnverifiedUser user, Password password) {
        if (user == null) {

            User verifiedUser = UserManager.getNullableUserOnline(user.getPlayer());

            if (verifiedUser == null) UserManager.addOnlineUser(user.getPlayer());

            return true;
        }

        Main.logInfo("Auto-registered player " + user.getPlayer().getName());

        //JavaScheduler.sync(user::autoLogin);

        return user.synchronizedAutoRegister(password);
    }

    @Override
    public boolean isRegistered(String s) {
        return UserFileManager.hasName(s);
    }*/
}