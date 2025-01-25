package shadow.systems.login.autoin;

import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumDataCache;
import alix.common.data.premium.PremiumStatus;
import alix.common.data.premium.name.PremiumNameManager;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import shadow.systems.login.autoin.premium.ClientPublicKey;
import ua.nanit.limbo.util.UUIDUtil;

import java.util.UUID;

public final class PremiumUtils {

    //https://github.com/kyngs/LibreLogin/blob/master/Plugin/src/main/java/xyz/kyngs/librelogin/common/premium/AuthenticPremiumProvider.java

    private static final PremiumCheckImpl premiumCheck = PremiumCheckImpl.INSTANCE;

    public static final char
            PREMIUM_PREFIX = '+',
            NON_PREMIUM_PREFIX = '-';

    public static String getPrefixedName(String name, boolean premium) {
        char prefix = premium ? PREMIUM_PREFIX : NON_PREMIUM_PREFIX;

        StringBuilder sb = new StringBuilder(name.length() + 1);
        sb.append(prefix).append(name);

        //we need to stay within the 16 character range to follow the mc protocol's limitations
        return sb.length() > 16 ? sb.substring(0, 16) : sb.toString();
    }

    public static String getNonPrefixedName(String name) {
        switch (name.charAt(0)) {
            case '+'://PREMIUM_PREFIX
            case '-'://NON_PREMIUM_PREFIX
                return name.length() == 16 ? PremiumNameManager.removeOriginalByPrefixed(name) : name.substring(1);
        }
        return name;
    }

    public static PremiumData getOrRequestAndCacheData(PersistentUserData data, String username) {
        PremiumData premiumData = getOrRequestData(data, username);
        //cache the username's premium status when it isn't unknown
        if (premiumData.getStatus().isKnown()) PremiumDataCache.add(username, premiumData);
        return premiumData;
    }

    public static PremiumData getOrRequestData(PersistentUserData data, String username) {
        PremiumData premiumData = getCachedData(data, username);

        return premiumData.getStatus().isKnown() ? premiumData : requestPremiumData(username);
    }

    public static PremiumData getCachedData(PersistentUserData data, String username) {
        return data != null ? data.getPremiumData() : PremiumDataCache.getOrUnknown(username);
    }

    public static PremiumStatus suggestsStatus(UUID uuid, ClientPublicKey clientPublicKey, ClientVersion version) {
        if (clientPublicKey != null) return PremiumStatus.PREMIUM;
        if (uuid == null || version.isOlderThan(ClientVersion.V_1_19)) return PremiumStatus.UNKNOWN;

        return uuid.version() == 4 ? PremiumStatus.PREMIUM : PremiumStatus.NON_PREMIUM;
    }

    public static PremiumData requestPremiumData(String name) {
        return premiumCheck.fetchPremiumData(name);
    }

    private static UUID fromString(String str) {
        return UUIDUtil.fromString(str);
    }
}