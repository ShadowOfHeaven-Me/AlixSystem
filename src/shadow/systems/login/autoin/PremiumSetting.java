package shadow.systems.login.autoin;

import alix.common.data.PersistentUserData;
import alix.common.utils.other.throwable.AlixError;
import shadow.Main;
import shadow.systems.login.autoin.premium.ClientPublicKey;

import java.util.UUID;

public enum PremiumSetting {
    NEVER,
    UNREGISTERED_AND_SUGGESTED,
    UNREGISTERED,
    ALWAYS;

    public static boolean requirePremium(PersistentUserData data, UUID uuid, ClientPublicKey clientPublicKey) {
        return requirePremium(PersistentUserData.isRegistered(data), uuid, clientPublicKey);
    }

    public static boolean requirePremium(boolean isRegistered, UUID uuid, ClientPublicKey clientPublicKey) {
        switch (Config.setting) {
            case NEVER:
                return false;
            case ALWAYS:
                return true;
            case UNREGISTERED_AND_SUGGESTED:
                return !isRegistered && (clientPublicKey != null
                        || uuid == null //require premium if the nickname is premium and the uuid was not sent (fallback for pre-1.19 clients)
                        || uuid.version() == 4);
            case UNREGISTERED:
                return !isRegistered;
            default:
                throw new AlixError("Invalid: " + Config.setting);
        }
    }

    private static final class Config {

        private static final PremiumSetting setting = config();

        private static PremiumSetting config() {
            String config = Main.config.getString("require-ownership-of-premium-nickname");
            try {
                return valueOf(config.toUpperCase());
            } catch (IllegalArgumentException e) {
                Main.logWarning("Invalid config parameter 'require-ownership-of-premium-nickname', got: '" + config + "', for \"never, unregistered, always\" being the only valid options!");
                return NEVER;
            }
        }
    }
}