package shadow.utils.users;

import org.bukkit.entity.Player;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

import java.util.UUID;

public final class Verifications {
    //private static final AlixMap<UUID, UnverifiedUser> map = new KeyedIdentityMap<>(Bukkit.getMaxPlayers());

    //not done async, because introducing a delay on packet blocking can prove fatal
    public static UnverifiedUser add(Player p, TemporaryUser tempUser, String joinMessage) {
        UnverifiedUser user = new UnverifiedUser(p, tempUser, joinMessage);
        UserManager.putUnv(user);
        return user;//Map#put returns the previous Value, thus it cannot be used instead for short
    }

    public static boolean has(Player p) {
        return has(p.getUniqueId());
    }

    public static boolean has(UUID uuid) {
        return !UserManager.get(uuid).isVerified();
    }

    public static UnverifiedUser get(Player p) {
        return get(p.getUniqueId());
    }

    public static UnverifiedUser get(UUID uuid) {
        AlixUser u = UserManager.get(uuid);
        return u.isVerified() ? null : (UnverifiedUser) u;
    }

/*    public static void forEach(BiConsumer<UUID, UnverifiedUser> consumer) {
        map.forEach(consumer);
    }*/

/*    public static void disable() {
        UserManager.users.forEach((i, u) -> !u.isVerified()
        map.forEach((i, u) -> u.disableActionBlockerAndUninject());
        //map.clear(); (?)
    }*/

    /*    static {
        //int size = AlixMap.calculateMapCapacity(Bukkit.getMaxPlayers());
        //map = new ConcurrentHashMap<>(Bukkit.getMaxPlayers());//concurrent because of captcha thread async user iteration
        map = new KeyedIdentityMap<>();
    }*/

}