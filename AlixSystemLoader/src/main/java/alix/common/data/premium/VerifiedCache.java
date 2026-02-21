package alix.common.data.premium;

import alix.common.data.PersistentUserData;
import alix.common.utils.AlixCache;
import com.github.retrooper.packetevents.manager.protocol.ProtocolManager;
import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.Channel;

import java.util.Map;

public final class VerifiedCache {

    private static final Map<String, User> verifiedNamesTempCache = AlixCache.newBuilder().maximumSize(100).weakValues().<String, User>build().asMap();

    public static void verify(String nickname, User user) {
        verifiedNamesTempCache.put(nickname, user);
    }

    public static boolean isPremium(PersistentUserData data, String name, User user) {
        return (data != null && data.getPremiumData().getStatus().isPremium() || getAndCheckIfEquals(name, user));
    }

    //Intentional identity checks

    public static boolean getAndCheckIfEquals(String nickname, Channel channel) {
        return getAndCheckIfEquals(nickname, ProtocolManager.USERS.get(channel.pipeline()));
    }

    public static boolean getAndCheckIfEquals(String nickname, User user) {
        return user == verifiedNamesTempCache.get(nickname);
    }

    public static boolean removeAndCheckIfEquals(String nickname, User user) {
        return user == verifiedNamesTempCache.remove(nickname);
    }
}