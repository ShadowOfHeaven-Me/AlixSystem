package shadow.systems.login.result;

import alix.common.utils.other.throwable.AlixError;
import io.netty.channel.Channel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.systems.netty.AlixChannelInjector;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LoginVerdictManager {

    private static final Map<String, LoginInfo> map = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());

    public static void addOffline(String name, String ip, PersistentUserData data) {
        put0(name, createVerdict(ip, data), ip, data);
    }

    public static void addOnline(String name, String ip, PersistentUserData data, boolean justCreated) {
        put0(name, justCreated ? LoginVerdict.REGISTER_PREMIUM : LoginVerdict.LOGIN_PREMIUM, ip, data);
    }

    private static final String packetHandlerName = "alix_handler";

    private static void put0(String name, LoginVerdict verdict, String ip, PersistentUserData data) {
        Channel channel = AlixChannelInjector.CHANNELS.remove(name);
        if (channel == null) throw new AlixError("Channel for " + name + " was not found!");
        channel.pipeline().remove(AlixChannelInjector.CHANNEL_ACTIVE_LISTENER_NAME);
        channel.pipeline().remove(AlixChannelInjector.PACKET_INJECTOR_NAME);

        PacketInterceptor interceptor = new PacketInterceptor(channel);
        channel.pipeline().addBefore("packet_handler", packetHandlerName, interceptor);
        map.put(name, new LoginInfo(verdict, ip, data, interceptor));
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