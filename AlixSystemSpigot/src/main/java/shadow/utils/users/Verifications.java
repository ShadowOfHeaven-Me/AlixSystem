package shadow.utils.users;

import org.bukkit.entity.Player;
import shadow.utils.main.AlixUtils;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;

import java.util.UUID;

public final class Verifications {
    //private static final AlixMap<UUID, UnverifiedUser> map = new KeyedIdentityMap<>(Bukkit.getMaxPlayers());

    //not done async, cuz no need
    public static UnverifiedUser add(Player p, TemporaryUser tempUser) {
        UnverifiedUser user = new UnverifiedUser(p, tempUser);
        UserManager.putUnv(user);
        return user;//Map#put returns the previous Value, thus it cannot be used instead for short
    }

    public static boolean has(Player p) {
        if (p == null)
            return false;

        AlixUser user = UserManager.get(p.getUniqueId());
        return user == null //uhhhhhhh
                && !AlixUtils.isFakePlayer(p) || !user.isVerified();
    }

    public static boolean remove(Player p) {
        return !UserManager.remove(p).isVerified();
    }

    public static UnverifiedUser get(Player p) {
        return get0(p.getUniqueId());
    }

    private static UnverifiedUser get0(UUID uuid) {
        return UserManager.get(uuid) instanceof UnverifiedUser unv ? unv : null;
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