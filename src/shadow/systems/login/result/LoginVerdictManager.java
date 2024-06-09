package shadow.systems.login.result;

import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import shadow.utils.main.AlixUtils;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;

import java.util.UUID;

public final class LoginVerdictManager {

    //private static final Map<String, LoginInfo> MAP = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(String name, String ip, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        put0(name, getVerdict(ip, data), ip, data, event);
    }

    public static void addOnline(String name, String ip, PersistentUserData data, boolean justCreated, AsyncPlayerPreLoginEvent event) {
        put0(name, justCreated ? LoginVerdict.REGISTER_PREMIUM : LoginVerdict.LOGIN_PREMIUM, ip, data, event);
    }

    //public static final String packetHandlerName = "alix_interception_handler";

    private static void put0(String name, LoginVerdict verdict, String ip, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        //Main.logInfo("[DEBUG] CREATING A VERDICT...");
/*        Channel channel = AlixChannelInjector.CHANNELS.remove(name);
        if (channel == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No channel)");
            Main.logWarning("Channel for " + name + " was not found!");//This most likely is a genuine issue, so it's better to log it
            return;
        }*/

        //ChannelPipeline pipeline = channel.pipeline();
        //PacketInterceptor interceptor = PacketInterceptor.construct(channel);

/*        if (interceptor == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No handler)");
            return;
        }*/
        User user = UserManager.removeConnecting(name);

        if (user == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No user)");
            return;
        }
        //Channel channel = (Channel) user.getChannel();
        /*try {
            //pipeline.addBefore("packet_handler", packetHandlerName, interceptor);
            AlixChannelHandler.uninject(channel.pipeline());
        } catch (Exception ex) {
            ex.printStackTrace();
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No login/native handler)");
            return;
        }*/

        LoginInfo info = new LoginInfo(verdict, ip, data);

        //MAP.put(name, info);
        UserManager.put(event.getUniqueId(), new TemporaryUser(user, info));
        //UserManager.
        //Main.logError("CREATED A VERDICT: " + info.getVerdict().readableName());
    }

    private static LoginVerdict getVerdict(String ip, PersistentUserData data) {
        if (data == null)//not registered - no data
            return LoginVerdict.DISALLOWED_NO_DATA;

        if (!data.getPassword().isSet())//not registered - password was reset
            return LoginVerdict.DISALLOWED_PASSWORD_RESET;

        if (!AlixUtils.forcefullyDisableIpAutoLogin && data.getLoginParams().getIpAutoLogin() && data.getSavedIP().equals(ip))//ip auto login
            return LoginVerdict.IP_AUTO_LOGIN;

        return LoginVerdict.DISALLOWED_LOGIN_REQUIRED; //not logged in
    }

    public static TemporaryUser get(Player p) {
        return get(p.getUniqueId());
    }

    public static TemporaryUser getNullable(UUID uuid) {
        AlixUser user = UserManager.get(uuid);
        return user instanceof TemporaryUser ? (TemporaryUser) user : null;//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)
    }

    public static TemporaryUser get(UUID uuid) {
        AlixUser user = UserManager.get(uuid);
        return ensureExists(user instanceof TemporaryUser ? (TemporaryUser) user : null);//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)
    }

/*    public static LoginInfo getExisting(Player p) {
        return getExistingTempUser(p).getLoginInfo();
    }*/

    private static TemporaryUser ensureExists(TemporaryUser user) {
        if (user == null) {
            user.reetrooperUser().closeConnection();
            //player.kickPlayer("§cNo Temporary User was found! Report this to the staff immediately!");
            throw new RuntimeException("No Temporary User was found for the player " + user.reetrooperUser().getName() + "! Report this as an error immediately! When reporting make sure to include the errors shown before this, if there were any!");
        }
        return user;
    }

    private LoginVerdictManager() {
    }
}