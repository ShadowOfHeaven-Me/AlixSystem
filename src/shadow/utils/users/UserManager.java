package shadow.utils.users;

import org.bukkit.entity.Player;
import shadow.systems.login.autoin.PremiumAutoIn;
import shadow.utils.objects.savable.data.PersistentUserData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserManager {

    private static final Map<UUID, User> users = new HashMap<>();
    //public static final List<User> users = new ArrayList<>();
    //public static final List<String> notVanishedUserNicknames = new ArrayList<>();

    public static void addOfflineUser(Player p, PersistentUserData data, String ip) { //offline as of non-premium
        if (data != null) add(new User(p, data.setIP(ip)));
        else throw new RuntimeException("No verified user data was provided!"); //add(new User(p, PersistentUserData.createDefault(p.getName(), ip)));
    }

    public static void addOnlineUser(Player p) { //online as of premium
        add(new User(p));
    }

/*    public static PersistentUserData register(Player p, Password password) {
        String ip = p.getAddress().getAddress().getHostAddress();
        //if (GeoIPTracker.disallowLogin(ip)) return false; //for now unnecessary

        PersistentUserData data = PersistentUserData.createDefault(p.getName(), ip);
        data.getPassword().setFrom(password);

        addOfflineUser(p, data, ip);

        return data;
    }*/

    public static PersistentUserData register(Player p, String password, String ip) {
        //if (GeoIPTracker.disallowLogin(ip)) return false;

        PersistentUserData data = PersistentUserData.createDefault(p.getName(), ip);
        data.setPassword(password);
        addOfflineUser(p, data, ip);

        return data;
    }

    private static void add(User u) {
        users.put(u.uuid, u);
        //PremiumAutoIn.remove(u.getName());
    }

/*    public static void disable() {
        if (isOfflineExecutorRegistered) {
            for (User u : users.values()) {
                u.quit();
            }
        }
    }*/

    public static void remove(Player p) {
        users.remove(p.getUniqueId());
    }

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
}