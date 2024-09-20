package shadow.systems.login.result;

import alix.common.data.PersistentUserData;
import alix.common.login.LoginVerdict;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import shadow.Main;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.UserManager;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;

import java.net.InetAddress;

public final class LoginVerdictManager {

    //private static final Map<String, LoginInfo> MAP = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(String name, String strIP, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        put0(name, strIP, getVerdict(event.getAddress(), data), data, event);
    }

    public static void addOnline(String name, String strIP, PersistentUserData data, boolean justCreated, AsyncPlayerPreLoginEvent event) {
        put0(name, strIP, justCreated ? LoginVerdict.REGISTER_PREMIUM : LoginVerdict.LOGIN_PREMIUM, data, event);
    }

    //public static final String packetHandlerName = "alix_interception_handler";

    private static void put0(String name, String strIP, LoginVerdict verdict, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
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

/*        Channel channel = (Channel) ProtocolManager.CHANNELS.get(event.getUniqueId());

        if (channel == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No channel)");
            return;
        }*/

        //fix for floodgate, could be erroneous
        //@ScheduledForFix
        //String prefix = Dependencies.FLOODGATE_PREFIX;
        String nameFixed = name; //prefix != null && name.startsWith(prefix) ? name.substring(prefix.length()) : name;

        User user = UserManager.removeConnecting(nameFixed);//name

        //Main.logError("NAME IN EVENT: " + name);

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

        LoginInfo info = new LoginInfo(verdict, event.getAddress(), strIP, data);

        //MAP.put(name, info);
        UserManager.put(event.getUniqueId(), new TemporaryUser(user, info));
        //UserManager.
        //Main.logError("CREATED A VERDICT: " + info.getVerdict().readableName());
    }

    private static LoginVerdict getVerdict(InetAddress ip, PersistentUserData data) {
        if (data == null)//not registered - no data
            return LoginVerdict.DISALLOWED_NO_DATA;

        if (!data.getPassword().isSet())//not registered - password was reset
            return LoginVerdict.DISALLOWED_PASSWORD_RESET;

        if (!AlixUtils.forcefullyDisableIpAutoLogin && data.getLoginParams().getIpAutoLogin() && data.getSavedIP().equals(ip))//ip auto login
            return LoginVerdict.IP_AUTO_LOGIN;

        return LoginVerdict.DISALLOWED_LOGIN_REQUIRED; //not logged in
    }

/*    public static TemporaryUser getNullable(UUID uuid) {
        AlixUser user = UserManager.get(uuid);
        return user instanceof TemporaryUser ? (TemporaryUser) user : null;//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)
    }*/

    //private static final ByteBuf noTempUserErr = OutDisconnectKickPacketConstructor.constructConstAtPlayPhase("§cSomething went wrong! (TempUser not assigned)");

    public static TemporaryUser get(Player p) {
        AlixUser user = UserManager.get(p.getUniqueId());
        TemporaryUser temp = user instanceof TemporaryUser ? (TemporaryUser) user : null;//the user can be null, thus instanceof is used instead of a class comparison (since the performance is almost identical in this case)
        if (temp == null) {
            if (user == null) {
                p.kickPlayer("§cSomething went wrong! (TempUser not assigned)");//cannot use MethodProvider.kickAsync, because AlixUser is null
                Main.logWarning("No Temporary User was found for the player " + p.getName() + " - disconnecting him for safety! Report this as an error immediately! When reporting make sure to include the errors shown before this, if there were any!");
            }
            return null;
        }
        return temp;
    }

/*    public static LoginInfo getExisting(Player p) {
        return getExistingTempUser(p).getLoginInfo();
    }*/

    private static TemporaryUser ensureExists(TemporaryUser user) {
        if (user == null) {
            //Why did I add this line previously
            //What was I smoking?
            //user.retrooperUser().closeConnection();
            throw new RuntimeException("No Temporary User was found for the player " + user.reetrooperUser().getName() + "! Report this as an error immediately! When reporting make sure to include the errors shown before this, if there were any!");
        }
        return user;
    }

    private LoginVerdictManager() {
    }
}