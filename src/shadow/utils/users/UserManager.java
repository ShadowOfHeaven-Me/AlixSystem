package shadow.utils.users;

import alix.common.data.Password;
import org.bukkit.entity.Player;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.objects.packet.PacketInterceptor;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserManager {

    private static final Map<UUID, User> users = new ConcurrentHashMap<>();
    //public static final List<User> users = new ArrayList<>();
    //public static final List<String> notVanishedUserNicknames = new ArrayList<>();

    public static void addOfflineUser(Player p, PersistentUserData data, String ip, PacketInterceptor handler) { //offline as of non-premium
        if (data != null) add0(new User(p, data.setIP(ip), handler));
        else throw new RuntimeException("No verified user data was provided!"); //add(new User(p, PersistentUserData.createDefault(p.getName(), ip)));
    }

    public static void addOnlineUser(Player p) { //online as of premium
        add0(new User(p));
    }

    public static void addOnlineUser(Player p, LoginInfo login) {//online as of premium
        add0(new User(p, login.getData(), login.getPacketInterceptor()));
    }

/*    public static Channel getPremiumChannel(User user) {

    }*/

/*    public static PersistentUserData register(Player p, Password password) {
        String ip = p.getAddress().getAddress().getHostAddress();
        //if (GeoIPTracker.disallowLogin(ip)) return false; //for now unnecessary

        PersistentUserData data = PersistentUserData.createDefault(p.getName(), ip);
        data.getPassword().setFrom(password);

        addOfflineUser(p, data, ip);

        return data;
    }*/

    public static PersistentUserData register(Player p, String password, String ip, PacketInterceptor handler) {
        //if (GeoIPTracker.disallowLogin(ip)) return false;

        PersistentUserData data = PersistentUserData.createDefault(p.getName(), ip, Password.fromUnhashed(password));
        //data.setPassword(password);
        addOfflineUser(p, data, ip, handler);

        return data;
    }

    private static void add0(User u) {
        users.put(u.getUUID(), u);
    }

/*    public static void disable() {
        if (isOfflineExecutorRegistered) {
            //users.values().forEach(User::quit);
            users.clear();
        }
    }*/

/*    public static void sendPlayerInfoToAll(String name) {
        //AlixScheduler.async(() -> users.forEach((i, u) -> u.duplexHandler.sendOf(name)));
    }*/

    public static User remove(Player p) {
        return users.remove(p.getUniqueId());
    }

/*    public static void remove(Player p) {
        User user = users.remove(p.getUniqueId());
        if (user != null) user.quit();
    }*/

    public static User getVerifiedUser(Player p) {
        User u = getNullableUserOnline(p);
        if (u == null) throw new RuntimeException("Null or unverified user access! - " + p.getName());
        return u;
    }

    public static User getNullableUserOnline(Player p) {
        return getNullableUserOnline(p.getUniqueId());
    }

    public static User getNullableUserOnline(UUID uuid) {
        return users.get(uuid);
    }

    private UserManager() {
    }
}