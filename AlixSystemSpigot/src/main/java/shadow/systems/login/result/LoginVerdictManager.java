package shadow.systems.login.result;

import alix.common.data.PersistentUserData;
import alix.common.login.LoginVerdict;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import shadow.Main;
import shadow.utils.main.AlixHandler;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;

import java.net.InetAddress;
import java.util.UUID;

public final class LoginVerdictManager {

    //private static final Map<String, LoginInfo> MAP = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(User user, String strIP, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        put0(user, strIP, getVerdict(event.getAddress(), data), data, event);
    }

    public static void addOnline(User user, String strIP, PersistentUserData data, boolean justCreated, AsyncPlayerPreLoginEvent event) {
        put0(user, strIP, justCreated ? LoginVerdict.REGISTER_PREMIUM : LoginVerdict.LOGIN_PREMIUM, data, event);
    }

    //public static final String packetHandlerName = "alix_interception_handler";

    private static void put0(User user, String strIP, LoginVerdict verdict, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        LoginInfo info = new LoginInfo(verdict, event.getAddress(), strIP, data);

        UserManager.put(event.getUniqueId(), new TemporaryUser(user, info));
    }

    private static final boolean isSessionExpiryEnabled = AlixUtils.autoLoginExpiry > 0;

    private static LoginVerdict getVerdict(InetAddress ip, PersistentUserData data) {
        if (data == null)//not registered - no data
            return LoginVerdict.DISALLOWED_NO_DATA;

        if (!data.getPassword().isSet())//not registered - password was reset
            return LoginVerdict.DISALLOWED_PASSWORD_RESET;

        if (!AlixUtils.forcefullyDisableIpAutoLogin && isSessionNotExpired(data) && data.getLoginParams().getIpAutoLogin() && data.getSavedIP().equals(ip))//ip auto login
            return LoginVerdict.IP_AUTO_LOGIN;

        return LoginVerdict.DISALLOWED_LOGIN_REQUIRED; //not logged in
    }

    private static boolean isSessionNotExpired(PersistentUserData data) {
        //Main.logError("SES " + sessionExpireEnabled + " LAST SUCC " + data.getLastSuccessfulLogin() + " EXPIRY " + AlixUtils.autoLoginExpiry + " TIME: " + System.currentTimeMillis());
        return !isSessionExpiryEnabled ||
                data.getLastSuccessfulLogin() + AlixUtils.autoLoginExpiry > System.currentTimeMillis();//sessionExpireEnabled must be true here
    }

/*    public static TemporaryUser getNullable(UUID uuid) {
        AlixUser user = UserManager.get(uuid);
        return user instanceof TemporaryUser ? (TemporaryUser) user : null;//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)
    }*/

    //private static final String noTempUserMsg = "§cSomething went wrong! (User not assigned)";
    //private static final ByteBuf noTempUserErr_Cfg = OutDisconnectPacketConstructor.dynamicAtConfig(noTempUserMsg);
    //private static final ByteBuf noTempUserErr_Play = OutDisconnectPacketConstructor.constAtPlay(noTempUserMsg);

    public static TemporaryUser get(UUID uuid) {
        AlixUser user = UserManager.get(uuid);
        return user instanceof TemporaryUser temp ? temp : null;
    }

    public static TemporaryUser getExisting(Player p) {
        AlixUser user = UserManager.get(p.getUniqueId());
        TemporaryUser temp = user instanceof TemporaryUser tem ? tem : null;//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)

        if (user == null) {
            //Cannot use Player::kickPlayer cuz Paper aids
            //Cannot use MethodProvider.kickAsync, because AlixUser is null
            Main.logWarning("No Alix User was found for the player " + p.getName() + " - disconnecting him for safety! Report this as an error immediately! When reporting make sure to include the errors shown before this, if there were any!");
            AlixHandler.safeKick(p,"§cSomething went wrong! (User not assigned)");
        }

        return temp;
    }

/*    public static LoginInfo getExisting(Player p) {
        return getExistingTempUser(p).getLoginInfo();
    }*/

    /*private static TemporaryUser ensureExists(TemporaryUser user) {
        if (user == null) {
            //Why did I add this line previously
            //What was I smoking?
            //user.retrooperUser().closeConnection();
            throw new RuntimeException("No Temporary User was found for the player " + user.retrooperUser().getName() + "! Report this as an error immediately! When reporting make sure to include the errors shown before this, if there were any!");
        }
        return user;
    }*/

    private LoginVerdictManager() {
    }
}