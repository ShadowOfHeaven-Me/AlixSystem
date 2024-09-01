package shadow.systems.login.autoin.fastlogin;

import alix.common.data.Password;
import alix.common.data.PersistentUserData;
import alix.common.data.file.UserFileManager;
import alix.common.login.LoginVerdict;
import alix.common.utils.other.throwable.AlixException;
import com.github.games647.fastlogin.core.hooks.AuthPlugin;
import org.bukkit.entity.Player;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

//https://github.com/games647/FastLogin/blob/main/core/src/main/java/com/github/games647/fastlogin/core/hooks/AuthPlugin.java
public final class AlixAuthFastLoginImpl implements AuthPlugin<Player> {

    @Override
    public boolean forceLogin(Player player) {
        //Main.logError("forceLogin");
        AlixUser user = UserManager.get(player.getUniqueId());

        if (user == null) throw new AlixException("Could not automatically log in player " + player.getName() + ", because the AlixUser object is null!"); //something went wrong

        if (user instanceof UnverifiedUser) ((UnverifiedUser) user).logIn();
        else if (user instanceof TemporaryUser) ((TemporaryUser) user).getLoginInfo().setVerdict(LoginVerdict.LOGIN_PREMIUM);

        return true;//was probably already auto-logged in by PremiumAutoIn
    }

    @Override
    public boolean forceRegister(Player player, String password) {
        //Main.logError("forceRegister");
        AlixUser user = UserManager.get(player.getUniqueId());

        if (user == null) throw new AlixException("Could not automatically register player " + player.getName() + ", because the AlixUser object is null!"); //something went wrong

        if (user instanceof UnverifiedUser) ((UnverifiedUser) user).registerAsync(password);
        else if (user instanceof TemporaryUser) {
            LoginInfo info = ((TemporaryUser) user).getLoginInfo();

            if (info.getData() == null)
                info.setData(PersistentUserData.createFromPremiumInfo(player.getName(),
                    player.getAddress().getAddress(),
                    Password.fromUnhashed(password)));
            //info.getData().setPassword(password);//
            info.setVerdict(LoginVerdict.REGISTER_PREMIUM);
        }

        return true;//was probably already auto-registered in by PremiumAutoIn
    }

    @Override
    public boolean isRegistered(String s) {
        //Main.logError("isRegistered");
        return UserFileManager.hasName(s);
    }
}