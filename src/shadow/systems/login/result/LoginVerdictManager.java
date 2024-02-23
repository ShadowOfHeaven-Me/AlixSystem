package shadow.systems.login.result;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerPreLoginEvent;
import shadow.Main;
import shadow.systems.netty.AlixChannelInjector;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginVerdictManager {

    private static final Map<String, LoginInfo> map = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(String name, String ip, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        put0(name, createVerdict(ip, data), ip, data, event);
    }

    public static void addOnline(String name, String ip, PersistentUserData data, boolean justCreated, AsyncPlayerPreLoginEvent event) {
        put0(name, justCreated ? LoginVerdict.REGISTER_PREMIUM : LoginVerdict.LOGIN_PREMIUM, ip, data, event);
    }

    public static final String packetHandlerName = "alix_handler";

    private static void put0(String name, LoginVerdict verdict, String ip, PersistentUserData data, AsyncPlayerPreLoginEvent event) {
        //Main.logInfo("[DEBUG] CREATING A VERDICT...");
        Channel channel = AlixChannelInjector.CHANNELS.remove(name);
        if (channel == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No channel)");
            Main.logWarning("Channel for " + name + " was not found!");
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        PacketInterceptor interceptor = PacketInterceptor.construct(channel);

        if (interceptor == null) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No handler)");
            return;
        }

        try {
            pipeline.addBefore("packet_handler", packetHandlerName, interceptor);
            pipeline.remove(AlixChannelInjector.PACKET_INJECTOR_NAME);
            pipeline.remove(AlixChannelInjector.CHANNEL_ACTIVE_LISTENER_NAME);
        } catch (NoSuchElementException ignored) {
            event.disallow(PlayerPreLoginEvent.Result.KICK_OTHER, "§cSomething went wrong! (No login or native handler)");
            return;
        }

        map.put(name, new LoginInfo(verdict, ip, data, interceptor));
        //Main.logInfo("[DEBUG] CREATED A VERDICT");
    }

    private static LoginVerdict createVerdict(String ip, PersistentUserData data) {
        if (data == null)//not registered - no data
            return LoginVerdict.DISALLOWED_NO_DATA;

        if (!data.getPassword().isSet())//not registered - password was reset
            return LoginVerdict.DISALLOWED_PASSWORD_RESET;

        if (data.getLoginParams().getIpAutoLogin() && data.getSavedIP().equals(ip))//ip auto login
            return LoginVerdict.IP_AUTO_LOGIN;

        return LoginVerdict.DISALLOWED_LOGIN_REQUIRED; //not logged in
    }

    public static LoginInfo remove(String name) {
        return map.remove(name);
    }

    public static LoginInfo removeExisting(Player p) {
        return ensureExists(p, remove(p.getName()));
    }

    public static LoginInfo getNullable(String name) {
        return map.get(name);
    }

    public static LoginInfo getExisting(Player p) {
        return ensureExists(p, getNullable(p.getName()));
    }

    private static LoginInfo ensureExists(Player player, LoginInfo info) {
        if (info == null) {
            player.kickPlayer("§cNo Login Verdict was found!");
            throw new RuntimeException("No Login Verdict was found for the player " + player.getName() + "! Report this as an error immediately! When reporting make sure to include the errors shown before this if there were any!");
        }
        return info;
    }

    private LoginVerdictManager() {
    }
}