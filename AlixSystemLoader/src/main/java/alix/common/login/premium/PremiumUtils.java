package alix.common.login.premium;

import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.premium.name.PremiumNameManager;
import alix.common.scheduler.AlixScheduler;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.channel.Channel;

import java.util.UUID;
import java.util.function.Consumer;

public final class PremiumUtils {

    //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/premium/AuthenticPremiumProvider.java

    private static final PremiumCheckImpl premiumCheck = PremiumCheckImpl.INSTANCE;
    private static final char
            PREMIUM_PREFIX = '+',
            NON_PREMIUM_PREFIX = '-';

    public static String getPrefixedName(String name, boolean premium) {
        char prefix = premium ? PREMIUM_PREFIX : NON_PREMIUM_PREFIX;

        StringBuilder sb = new StringBuilder(name.length() + 1);
        sb.append(prefix).append(name);

        //we need to stay within the 16 character range in order to follow the mc protocol's limitations
        return sb.length() > 16 ? sb.substring(0, 16) : sb.toString();
    }

    public static String getNonPrefixedName(String name) {
        switch (name.charAt(0)) {
            case PREMIUM_PREFIX, NON_PREMIUM_PREFIX:
                return name.length() == 16 ? PremiumNameManager.removeOriginalByPrefixed(name) : name.substring(1);
        }
        return name;
    }

    public static void getOrRequestAndCacheData(Channel channel, String username, Consumer<PremiumData> consumer) {
        getOrRequestAndCacheData(channel, null, username, consumer);
    }

    public static void getOrRequestAndCacheData(Channel channel, PersistentUserData data, String username, Consumer<PremiumData> consumer) {
        PremiumData userData = data != null ? data.getPremiumData() : PremiumData.UNKNOWN;
        if (userData.getStatus().isKnown()) {
            consumer.accept(userData);
            return;
        }

        PremiumData cached = PremiumDataCache.getOrUnknown(username);
        if (cached.getStatus().isKnown()) {
            consumer.accept(cached);
            return;
        }

        AlixScheduler.asyncBlocking(() -> {
            PremiumData premiumData = requestPremiumData(username);

            //cache the username's premium status when it isn't unknown
            if (premiumData.getStatus().isKnown())
                PremiumDataCache.add(username, premiumData);

            if (channel != null)
                channel.eventLoop().execute(() -> consumer.accept(premiumData));
            else
                consumer.accept(premiumData);
        });
    }

    public static PremiumData getOrRequestAndCacheDataSync(PersistentUserData data, String username) {
        PremiumData userData = data != null ? data.getPremiumData() : PremiumData.UNKNOWN;
        if (userData.getStatus().isKnown()) return userData;

        PremiumData cached = PremiumDataCache.getOrUnknown(username);
        if (cached.getStatus().isKnown()) return cached;

        PremiumData premiumData = requestPremiumData(username);

        //cache the username's premium status when it isn't unknown
        if (premiumData.getStatus().isKnown())
            PremiumDataCache.add(username, premiumData);

        return premiumData;
    }

    public static PremiumStatus suggestsStatus(UUID uuid, ClientVersion version) {
        //Not sure about this one
        //if (clientPublicKey != null) return PremiumStatus.PREMIUM;
        if (uuid == null || version.isOlderThan(ClientVersion.V_1_19)) return PremiumStatus.UNKNOWN;

        return uuid.version() == 4 ? PremiumStatus.PREMIUM : PremiumStatus.NON_PREMIUM;
    }

    static PremiumData requestPremiumData(String name) {
        return premiumCheck.fetchPremiumData(name);
    }
}