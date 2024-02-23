package shadow.systems.login;

import org.bukkit.entity.Player;
import shadow.systems.login.result.LoginInfo;
import shadow.utils.objects.savable.data.PersistentUserData;
import shadow.utils.users.offline.UnverifiedUser;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Verifications {//TODO: Commands and users

    private static final Map<UUID, UnverifiedUser> map = new ConcurrentHashMap<>();
    //private static final AlixMap<UUID, UnverifiedUser> map = new KeyedIdentityMap<>(Bukkit.getMaxPlayers());

    //not done async, because introducing a delay on packet blocking can prove fatal
    public static UnverifiedUser add(Player p, LoginInfo login, String joinMessage) {
        UnverifiedUser user = new UnverifiedUser(p, login, joinMessage);
        map.put(p.getUniqueId(), user);
        return user;//Map#put returns the previous Value, thus it cannot be used instead for short
    }

    public static UnverifiedUser remove(Player p) {
        return map.remove(p.getUniqueId());
    }

    public static boolean has(Player p) {
        return get(p) != null;
    }

    public static boolean has(UUID uuid) {
        return get(uuid) != null;
    }

    public static UnverifiedUser get(Player p) {
        return map.get(p.getUniqueId());
    }

    public static UnverifiedUser get(UUID uuid) {
        return map.get(uuid);
    }

    public static Iterable<UnverifiedUser> users() {
        return map.values();
    }

/*    public static void forEach(BiConsumer<UUID, UnverifiedUser> consumer) {
        map.forEach(consumer);
    }*/

    public static void disable() {
        map.forEach((i, u) -> u.disableActionBlockerAndUninject());
        //map.clear(); (?)
    }

    /*    static {
        //int size = AlixMap.calculateMapCapacity(Bukkit.getMaxPlayers());
        //map = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());//concurrent because of captcha thread async user iteration
        map = new KeyedIdentityMap<>();
    }*/

}