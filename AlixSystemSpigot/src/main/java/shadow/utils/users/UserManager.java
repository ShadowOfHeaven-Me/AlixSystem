package shadow.utils.users;

import alix.common.data.PersistentUserData;
import alix.common.data.security.password.Password;
import alix.common.utils.AlixCommonUtils;
import alix.common.utils.other.throwable.AlixException;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.ChannelHandlerContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import shadow.utils.users.types.AlixUser;
import shadow.utils.users.types.TemporaryUser;
import shadow.utils.users.types.UnverifiedUser;
import shadow.utils.users.types.VerifiedUser;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class UserManager {

    private static final Map<UUID, AlixUser> USERS = new ConcurrentHashMap<>(Bukkit.getMaxPlayers() << 1);//always use more buckets to lessen the chance of a hash collision
    private static final Map<String, User> CONNECTING_USERS = new ConcurrentHashMap<>();//the default size of 16 will do, since the joining players are only stored here since connection till login start
    //public static final List<User> users = new ArrayList<>();
    //public static final List<String> notVanishedUserNicknames = new ArrayList<>();

/*    public static TemporaryUser tempUser(UUID uuid) {
        return users.
    }*/

    public static VerifiedUser addVerifiedUser(Player p, TemporaryUser user, Consumer<VerifiedUser> onFirstPlayPacket) {
        return putVer(new VerifiedUser(p, user, onFirstPlayPacket));
    }

    public static VerifiedUser addVerifiedUser(Player p, PersistentUserData data, InetAddress ip, User retrooperUser, ChannelHandlerContext silentContext) {//offline as of non-premium
        Objects.requireNonNull(data, "No verified user data was provided!");
        return putVer(new VerifiedUser(p, data.setIP(ip), retrooperUser, silentContext, AlixCommonUtils.EMPTY_CONSUMER));
        //add(new User(p, PersistentUserData.createDefault(p.getName(), ip)));
    }

    /*public static void addOnlineUser(Player p) { //online as of premium
        putVer(new VerifiedUser(p));
    }*/

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

    public static VerifiedUser register(Player p, String password, InetAddress ip, User user, ChannelHandlerContext silentContext) {
        //if (GeoIPTracker.disallowLogin(ip)) return false;

        PersistentUserData data = PersistentUserData.createDefault(p.getName(), ip, Password.fromUnhashed(password));
        //data.setPassword(password);

        return addVerifiedUser(p, data, ip, user, silentContext);
    }

    private static VerifiedUser putVer(VerifiedUser u) {
        USERS.put(u.getUUID(), u);
        u.getData().updateLastSuccessfulLoginTime();
        return u;
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

    //Optimization Util - start

/*    private static final AttributeKey<AlixUser> USER_ATTR = AttributeKey.valueOf("alix-user");

    public static void putAttr(AlixUser user) {
        user.getChannel().attr(USER_ATTR).set(user);
    }

    public static AlixUser getAttr(Channel channel) {
        return channel.attr(USER_ATTR).get();
    }*/

    //Optimization Util - end

    static void putUnv(UnverifiedUser user) {
        USERS.put(user.getPlayer().getUniqueId(), user);
    }

    public static void put(UUID uuid, AlixUser user) {
        //Main.logError("PUT USER WITH UUID " + uuid + " " + user.getClass().getSimpleName());
        USERS.put(uuid, user);
    }

    public static AlixUser get(UUID uuid) {
        return USERS.get(uuid);
    }

    public static AlixUser remove(Player p) {
        return USERS.remove(p.getUniqueId());
    }

    public static User putConnecting(String name, User user) {
        return CONNECTING_USERS.compute(name, (n, alreadyConnecting) -> alreadyConnecting != null ? alreadyConnecting : user);
    }

    public static User getConnecting(String name) {
        return CONNECTING_USERS.get(name);
    }

    public static User removeConnecting(String name) {
        return CONNECTING_USERS.remove(name);
    }

/*    public static void remove(Player p) {
        User user = users.remove(p.getUniqueId());
        if (user != null) user.quit();
    }*/

    public static VerifiedUser getVerifiedUser(Player p) {
        VerifiedUser u = getNullableVerifiedUser(p);
        if (u == null) throw new AlixException("Null or unverified user access! - " + p.getName());
        return u;
    }

    public static VerifiedUser getNullableVerifiedUser(Player p) {
        return getNullableVerifiedUser(p.getUniqueId());
    }

    public static VerifiedUser getNullableVerifiedUser(UUID uuid) {
        AlixUser user = USERS.get(uuid);
        return user instanceof VerifiedUser ? (VerifiedUser) user : null;
    }

    /*public static int userCount() {
        return USERS.size();
    }*/

    public static Collection<AlixUser> users() {
        return USERS.values();
    }

    private UserManager() {
    }
}