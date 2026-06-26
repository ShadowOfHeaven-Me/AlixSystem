package alix.common.login.premium;

import alix.common.AlixCommonMain;
import alix.common.data.PersistentUserData;
import alix.common.data.premium.PremiumData;
import alix.common.data.premium.PremiumStatus;
import alix.common.utils.other.throwable.AlixError;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import io.netty.channel.Channel;

import java.util.UUID;
import java.util.function.Consumer;

import static alix.common.utils.config.ConfigProvider.config;

public enum PremiumSetting {
    NEVER,
    UNREGISTERED_AND_SUGGESTED,
    UNREGISTERED,
    ALWAYS;


    public static void performPremiumCheckNullData(Channel channel, String name, UUID uuid, ClientPublicKey clientPublicKey, ClientVersion version,
                                                   Consumer<PremiumData> consumer) {
        if (!PremiumSetting.requirePremium(false, uuid, clientPublicKey, version)) {
            consumer.accept(PremiumData.UNKNOWN);
            return;
        }

        PremiumUtils.getOrRequestAndCacheData(channel, null, name, consumer);
    }

    public static void performPremiumCheck(Channel channel, PersistentUserData data, String name, UUID uuid, ClientPublicKey clientPublicKey,
                                           ClientVersion version, Consumer<PremiumData> consumer) {
        switch (data.getPremiumData().getStatus()) {
            case PREMIUM, NON_PREMIUM:
                consumer.accept(data.getPremiumData());
                return;
            case UNKNOWN:
                if (!PremiumSetting.requirePremium(data, uuid, clientPublicKey, version)) {
                    consumer.accept(PremiumData.UNKNOWN);
                    return;
                }
                PremiumUtils.getOrRequestAndCacheData(channel, data, name, newData -> {
                    data.setPremiumData(newData);
                    consumer.accept(newData);
                });
            default:
                throw new AlixError("Invalid: " + data.getPremiumData().getStatus());
        }
    }

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

        private static final boolean requirePremiumWhenNoSuggestion = config.getBoolean("require-premium-when-no-suggestion");
        private static final PremiumSetting setting = config();

        private static PremiumSetting config() {
            String cfg = config.getString("require-ownership-of-premium-nickname");
            try {
                return valueOf(cfg.toUpperCase());
            } catch (IllegalArgumentException e) {
                AlixCommonMain.logWarning("Invalid config parameter 'require-ownership-of-premium-nickname', got: '" + cfg + "', for \"never, unregistered_and_suggested, unregistered, always\" being the only valid options!");
                return UNREGISTERED_AND_SUGGESTED;//config default
            }
        }
    }
}