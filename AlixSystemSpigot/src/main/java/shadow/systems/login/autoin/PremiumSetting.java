package shadow.systems.login.autoin;

import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumStatus;
import alix.common.login.premium.PremiumUtils;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import shadow.Main;
import alix.common.login.premium.ClientPublicKey;

import java.util.UUID;

public enum PremiumSetting {
    NEVER,
    UNREGISTERED_AND_SUGGESTED,
    UNREGISTERED,
    ALWAYS;

    public static boolean isPremium(PremiumStatus status) {
        switch (status) {
            case NON_PREMIUM:
                return false;
            case PREMIUM:
                return true;
            case UNKNOWN://require premium? if the nickname is premium and the uuid was not sent (fallback for pre-1.19 clients)
                return Config.requirePremiumWhenNoSuggestion;
            default:
                throw new AlixError("how");
        }
    }

    public static boolean requirePremium(PersistentUserData data, UUID uuid, ClientPublicKey clientPublicKey, ClientVersion version) {
        return requirePremium(PersistentUserData.isRegistered(data), uuid, clientPublicKey, version);
    }

    public static boolean requirePremium(boolean isRegistered, UUID uuid, ClientPublicKey clientPublicKey, ClientVersion version) {
        return requirePremium(isRegistered, PremiumUtils.suggestsStatus(uuid, clientPublicKey, version));
    }

    public static boolean requirePremium(boolean isRegistered, PremiumStatus suggestsStatus) {
        switch (Config.setting) {
            case NEVER:
                return false;
            case ALWAYS:
                return true;
            case UNREGISTERED:
                return !isRegistered;
            case UNREGISTERED_AND_SUGGESTED:
                if (isRegistered) return false;

                return isPremium(suggestsStatus);
            default:
                throw new AlixError("Invalid: " + Config.setting);
        }
    }

    private static final class Config {

        private static final boolean requirePremiumWhenNoSuggestion = Main.config.getBoolean("require-premium-when-no-suggestion");
        private static final PremiumSetting setting = config();

        private static PremiumSetting config() {
            String config = Main.config.getString("require-ownership-of-premium-nickname");
            try {
                return valueOf(config.toUpperCase());
            } catch (IllegalArgumentException e) {
                Main.logWarning("Invalid config parameter 'require-ownership-of-premium-nickname', got: '" + config + "', for \"never, unregistered_and_suggested, unregistered, always\" being the only valid options!");
                return UNREGISTERED_AND_SUGGESTED;//config default
            }
        }
    }
}